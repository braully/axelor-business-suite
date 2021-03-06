/**
 * Axelor Business Solutions
 *
 * Copyright (C) 2014 Axelor (<http://axelor.com>).
 *
 * This program is free software: you can redistribute it and/or  modify
 * it under the terms of the GNU Affero General Public License, version 3,
 * as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.axelor.apps.supplychain.service;

import java.util.ArrayList;
import java.util.List;

import org.joda.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Currency;
import com.axelor.apps.base.db.IProduct;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.PriceList;
import com.axelor.apps.base.db.Product;
import com.axelor.auth.db.User;
import com.axelor.apps.purchase.db.PurchaseOrder;
import com.axelor.apps.purchase.db.PurchaseOrderLine;
import com.axelor.apps.purchase.service.PurchaseOrderLineService;
import com.axelor.apps.purchase.service.PurchaseOrderServiceImpl;
import com.axelor.apps.stock.db.ILocation;
import com.axelor.apps.stock.db.StockConfig;
import com.axelor.apps.stock.service.StockMoveLineService;
import com.axelor.apps.stock.service.StockMoveService;
import com.axelor.apps.stock.service.config.StockConfigService;
import com.axelor.apps.stock.db.Location;
import com.axelor.apps.stock.db.StockMove;
import com.axelor.apps.stock.db.StockMoveLine;
import com.axelor.apps.stock.db.repo.LocationRepository;
import com.axelor.apps.stock.db.repo.StockMoveRepository;
import com.axelor.apps.supplychain.exception.IExceptionMessage;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.IException;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;

public class PurchaseOrderServiceSupplychainImpl extends PurchaseOrderServiceImpl {

	private static final Logger LOG = LoggerFactory.getLogger(PurchaseOrderServiceSupplychainImpl.class); 

	public PurchaseOrder createPurchaseOrder(User buyerUser, Company company, Partner contactPartner, Currency currency, 
			LocalDate deliveryDate, String internalReference, String externalReference, int invoicingTypeSelect, Location location, LocalDate orderDate, 
			PriceList priceList, Partner supplierPartner) throws AxelorException  {
		
		LOG.debug("Création d'une commande fournisseur : Société = {},  Reference externe = {}, Fournisseur = {}",
				new Object[] { company.getName(), externalReference, supplierPartner.getFullName() });
		
		PurchaseOrder purchaseOrder = super.createPurchaseOrder(buyerUser, company, contactPartner, currency, deliveryDate, 
				internalReference, externalReference, invoicingTypeSelect, orderDate, priceList, supplierPartner);
				
		purchaseOrder.setLocation(location);
		purchaseOrder.setInvoicingTypeSelect(invoicingTypeSelect);
		
		return purchaseOrder;
	}
	
	
	
	/**
	 * Méthode permettant de créer un StockMove à partir d'un PurchaseOrder.
	 * @param purchaseOrder une commande
	 * @throws AxelorException Aucune séquence de StockMove n'a été configurée
	 */
	public void createStocksMoves(PurchaseOrder purchaseOrder) throws AxelorException {
		
		if(purchaseOrder.getPurchaseOrderLineList() != null && purchaseOrder.getCompany() != null) {
			StockConfigService stockConfigService = Beans.get(StockConfigService.class);
			Company company = purchaseOrder.getCompany();
			
			StockConfig stockConfig = stockConfigService.getStockConfig(company);
			
			Location startLocation = Beans.get(LocationRepository.class).findByPartner(purchaseOrder.getSupplierPartner());
			
			if(startLocation == null)  {
				startLocation = stockConfigService.getSupplierVirtualLocation(stockConfig);
			}
			if(startLocation == null)  {
				throw new AxelorException(String.format(I18n.get(IExceptionMessage.PURCHASE_ORDER_1), 
						company.getName()), IException.CONFIGURATION_ERROR);
			}
			
			Partner supplierPartner = purchaseOrder.getSupplierPartner();

			StockMove stockMove = Beans.get(StockMoveService.class).createStockMove(supplierPartner.getDeliveryAddress(), null, company, supplierPartner, startLocation, purchaseOrder.getLocation(), purchaseOrder.getDeliveryDate());
			stockMove.setPurchaseOrder(purchaseOrder);
			stockMove.setStockMoveLineList(new ArrayList<StockMoveLine>());
			
			for(PurchaseOrderLine purchaseOrderLine: purchaseOrder.getPurchaseOrderLineList()) {
				
				Product product = purchaseOrderLine.getProduct();
				// Check if the company field 'hasInSmForStorableProduct' = true and productTypeSelect = 'storable' or 'hasInSmForNonStorableProduct' = true and productTypeSelect = 'service' or productTypeSelect = 'other'
				if(product != null && ((stockConfig.getHasInSmForStorableProduct() && product.getProductTypeSelect().equals(IProduct.PRODUCT_TYPE_STORABLE)) 
						|| (stockConfig.getHasInSmForNonStorableProduct() && !product.getProductTypeSelect().equals(IProduct.PRODUCT_TYPE_STORABLE)))) {

					StockMoveLine stockMoveLine = Beans.get(StockMoveLineService.class).createStockMoveLine(product, purchaseOrderLine.getQty(), purchaseOrderLine.getUnit(), 
							Beans.get(PurchaseOrderLineService.class).computeDiscount(purchaseOrderLine), stockMove, 2);
					if(stockMoveLine != null) {
						
						stockMoveLine.setPurchaseOrderLine(purchaseOrderLine);
						
						stockMove.getStockMoveLineList().add(stockMoveLine);
					}
				}	
			}
			if(stockMove.getStockMoveLineList() != null && !stockMove.getStockMoveLineList().isEmpty()){
				Beans.get(StockMoveService.class).plan(stockMove);
			}
		}
	}
	
	
	public Location getLocation(Company company)  {
		
		return Beans.get(LocationRepository.class).all().filter("self.company = ?1 and self.isDefaultLocation = ?2 and self.typeSelect = ?3", company, true, ILocation.INTERNAL).fetchOne();
	}
	
	
	public void clearPurchaseOrder(PurchaseOrder purchaseOrder) throws AxelorException  {
		
		List<StockMove> stockMoveList = (List<StockMove>) Beans.get(StockMoveRepository.class).all().filter("self.purchaseOrder = ?1 AND self.statusSelect = 2", purchaseOrder).fetch();
		
		for(StockMove stockMove : stockMoveList)  {
			
			Beans.get(StockMoveService.class).cancel(stockMove);
			
		}
		
		
	}
	
	
	@Override
	public void _computePurchaseOrder(PurchaseOrder purchaseOrder) throws AxelorException {
		
		super._computePurchaseOrder(purchaseOrder);
		
		purchaseOrder.setAmountRemainingToBeInvoiced(purchaseOrder.getInTaxTotal());
		
	}
	
	
	
}
