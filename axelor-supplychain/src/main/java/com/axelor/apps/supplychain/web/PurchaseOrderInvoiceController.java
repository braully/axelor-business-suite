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
import com.axelor.apps.purchase.db.PurchaseOrder;
import com.axelor.apps.supplychain.exception.IExceptionMessage;
import com.axelor.apps.supplychain.service.PurchaseOrderInvoiceService;
import com.axelor.exception.service.TraceBackService;
import com.axelor.i18n.I18n;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.inject.Inject;

public class PurchaseOrderInvoiceController {

	@Inject
	private PurchaseOrderInvoiceService purchaseOrderInvoiceService;
	
	public void generateInvoice(ActionRequest request, ActionResponse response)  {
		
		PurchaseOrder purchaseOrder = request.getContext().asType(PurchaseOrder.class);
		
		try {
			Invoice invoice = purchaseOrderInvoiceService.generateInvoice(purchaseOrder);
			
			if(invoice != null)  {
				response.setReload(true);
				response.setFlash(I18n.get(IExceptionMessage.PO_INVOICE_2));
			}
		}
		catch(Exception e)  { TraceBackService.trace(response, e); }
	}
}
