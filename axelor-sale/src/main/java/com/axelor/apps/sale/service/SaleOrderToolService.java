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
package com.axelor.apps.sale.service;

import java.math.BigDecimal;
import java.math.RoundingMode;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.apps.base.db.IAdministration;
import com.axelor.apps.base.service.CurrencyService;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.exception.AxelorException;
import com.google.inject.Inject;

public class SaleOrderToolService {

	private static final Logger LOG = LoggerFactory.getLogger(SaleOrderToolService.class); 
	
	@Inject
	private CurrencyService currencyService;
	
	
	/**
	 * Calculer le montant HT d'une ligne de devis.
	 * 
	 * @param quantity
	 *          Quantité.
	 * @param price
	 *          Le prix.
	 * 
	 * @return 
	 * 			Le montant HT de la ligne.
	 */
	public BigDecimal computeAmount(BigDecimal quantity, BigDecimal price) {

		BigDecimal amount = quantity.multiply(price).setScale(IAdministration.DEFAULT_NB_DECIMAL_DIGITS, RoundingMode.HALF_EVEN);

		LOG.debug("Calcul du montant HT avec une quantité de {} pour {} : {}", new Object[] { quantity, price, amount });

		return amount;
	}
	
	
	public BigDecimal getAccountingExTaxTotal(BigDecimal exTaxTotal, SaleOrder saleOrder) throws AxelorException  {
		
		return currencyService.getAmountCurrencyConverted(
				saleOrder.getCurrency(), saleOrder.getClientPartner().getCurrency(), exTaxTotal, saleOrder.getCreationDate()).setScale(IAdministration.DEFAULT_NB_DECIMAL_DIGITS, RoundingMode.HALF_UP);  
	}
	
}
