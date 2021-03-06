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

import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.account.service.IrrecoverableService;
import com.axelor.apps.account.service.MoveLineService;
import com.axelor.exception.service.TraceBackService;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.inject.Inject;
import com.google.inject.Injector;

public class MoveLineController {
	
	@Inject
	private Injector injector;
	
	@Inject
	private MoveLineService moveLineService;
	
	public void usherProcess(ActionRequest request, ActionResponse response) {
		
		MoveLine moveLine = request.getContext().asType(MoveLine.class);
		moveLine = moveLineService.find(moveLine.getId());
		
		MoveLineService mls = injector.getInstance(MoveLineService.class);
		
		try {
			mls.usherProcess(moveLine);
		}
		catch (Exception e){ TraceBackService.trace(response, e); }
	}
	
	public void passInIrrecoverable(ActionRequest request, ActionResponse response)  {
		
		MoveLine moveLine = request.getContext().asType(MoveLine.class);
		moveLine = moveLineService.find(moveLine.getId());
		
		IrrecoverableService is = injector.getInstance(IrrecoverableService.class);
		
		try  {
			is.passInIrrecoverable(moveLine, true, true);
			response.setReload(true);
		}
		catch(Exception e)  { TraceBackService.trace(response, e); }
	}
	
	public void notPassInIrrecoverable(ActionRequest request, ActionResponse response)  {
		
		MoveLine moveLine = request.getContext().asType(MoveLine.class);
		moveLine = moveLineService.find(moveLine.getId());
		
		IrrecoverableService is = injector.getInstance(IrrecoverableService.class);
		
		try  {
			is.notPassInIrrecoverable(moveLine, true);
			response.setReload(true);
		}
		catch(Exception e)  { TraceBackService.trace(response, e); }
	}
}
