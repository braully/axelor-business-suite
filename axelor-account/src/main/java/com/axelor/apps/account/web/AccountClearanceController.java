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
package com.axelor.apps.account.web;

import java.util.HashMap;
import java.util.Map;

import com.axelor.apps.account.db.AccountClearance;
import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.account.exception.IExceptionMessage;
import com.axelor.apps.account.service.AccountClearanceService;
import com.axelor.exception.service.TraceBackService;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.axelor.rpc.Context;

public class AccountClearanceController {

	
	public void getExcessPayment(ActionRequest request, ActionResponse response)  {
		
		AccountClearance accountClearance = request.getContext().asType(AccountClearance.class);
		
		try {	
			Beans.get(AccountClearanceService.class).setExcessPayment(accountClearance);
			response.setReload(true);		
		}
		catch(Exception e)  { TraceBackService.trace(response, e); }		
	}
	
	public void validateAccountClearance(ActionRequest request, ActionResponse response)  {
		
		AccountClearanceService accountClearanceService = Beans.get(AccountClearanceService.class);
		AccountClearance accountClearance = request.getContext().asType(AccountClearance.class);
		accountClearance = accountClearanceService.find(accountClearance.getId());
		
		try {
			accountClearanceService.validateAccountClearance(accountClearance);
			response.setReload(true);		
		}
		catch(Exception e)  { TraceBackService.trace(response, e); }
	}
	
	public void showAccountClearanceMoveLines(ActionRequest request, ActionResponse response)  {
		
		Map<String,Object> viewMap = new HashMap<String,Object>();
		
		Context context = request.getContext();
		viewMap.put("title", I18n.get(IExceptionMessage.ACCOUNT_CLEARANCE_7));
		viewMap.put("resource", MoveLine.class.getName());
		viewMap.put("domain", "self.accountClearance.id = "+context.get("id"));
		response.setView(viewMap);
	}
}
