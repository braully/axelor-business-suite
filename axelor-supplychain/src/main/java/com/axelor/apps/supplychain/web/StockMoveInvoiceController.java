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
package com.axelor.apps.supplychain.web;

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.stock.db.StockMove;
import com.axelor.apps.stock.db.repo.StockMoveRepository;
import com.axelor.apps.supplychain.exception.IExceptionMessage;
import com.axelor.apps.supplychain.service.StockMoveInvoiceService;
import com.axelor.exception.service.TraceBackService;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.inject.Inject;

public class StockMoveInvoiceController {

	@Inject
	private StockMoveInvoiceService stockMoveInvoiceService;
	
	public void generateInvoice(ActionRequest request, ActionResponse response)  {
		
		StockMove stockMove = request.getContext().asType(StockMove.class);
		Invoice invoice = null;
		try {
			stockMove = Beans.get(StockMoveRepository.class).find(stockMove.getId());
			
			if(stockMove.getSaleOrder() != null) {
				invoice = stockMoveInvoiceService.createInvoiceFromSaleOrder(stockMove, stockMove.getSaleOrder());
			}
			
			if(stockMove.getPurchaseOrder() != null) {
				invoice = stockMoveInvoiceService.createInvoiceFromPurchaseOrder(stockMove, stockMove.getPurchaseOrder());
			}
			
			if(invoice != null)  {
				response.setReload(true);
				response.setFlash(I18n.get(IExceptionMessage.PO_INVOICE_2));
			}
		}
		catch(Exception e)  { TraceBackService.trace(response, e); }
	}
}
