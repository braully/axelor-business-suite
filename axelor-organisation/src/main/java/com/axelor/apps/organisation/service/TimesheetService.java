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
package com.axelor.apps.organisation.service;

import java.util.List;

import javax.persistence.Query;

import com.axelor.apps.base.db.SpentTime;
import com.axelor.apps.base.service.UnitConversionService;
import com.axelor.apps.organisation.db.Task;
import com.axelor.apps.organisation.db.Timesheet;
import com.axelor.apps.organisation.db.TimesheetLine;
import com.axelor.apps.organisation.db.repo.TimesheetRepository;
import com.axelor.auth.db.User;
import com.axelor.db.JPA;
import com.axelor.exception.AxelorException;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.persist.Transactional;

public class TimesheetService extends TimesheetRepository{
	
	@Inject
	private Injector injector;
	
	@Inject
	private SpentTimeService spentTimeService;
	
	
	@Transactional(rollbackOn = {AxelorException.class, Exception.class})
	public void getTaskSpentTime(Timesheet timesheet) throws AxelorException  {
		
		User user = timesheet.getUser();
		
		Query q = JPA.em().createQuery("select DISTINCT(task) FROM SpentTime as pt WHERE pt.user = ?1 AND pt.timesheetImputed IN (false,null)");
		q.setParameter(1, user);
				
		@SuppressWarnings("unchecked")
		List<Task> taskList = q.getResultList();
		
		for(Task task : taskList)  {
			
			timesheet.getTimesheetLineList().addAll(this.createTimesheetLines(timesheet, task));
			
		}
		save(timesheet);
		
	}
	
	public List<TimesheetLine> createTimesheetLines(Timesheet timesheet, Task task) throws AxelorException  {
		
		List<TimesheetLine> timesheetLineList = Lists.newArrayList();
		
		List<SpentTime> spentTimeList = (List<SpentTime>) spentTimeService.all().filter("self.user = ?1 AND self.task = ?2  AND self.timesheetImputed IN (false,null)", timesheet.getUser(), task).fetch();
		
		for(SpentTime spentTime : spentTimeList)  {
			
			timesheetLineList.add(this.createTimesheetLine(timesheet, task, spentTime));
			
			spentTime.setTimesheetImputed(true);
			spentTimeService.save(spentTime);
		}
		
		return timesheetLineList;
	}
	
	
	public TimesheetLine createTimesheetLine(Timesheet timesheet, Task task, SpentTime spentTime) throws AxelorException  {
		
		TimesheetLine timesheetLine = new TimesheetLine();
		timesheetLine.setTimesheet(timesheet);
		
		timesheetLine.setProject(task.getProject());
		timesheetLine.setTask(task);
		timesheetLine.setIsToInvoice(true);
		timesheetLine.setDate(spentTime.getDate());
		
		timesheetLine.setDuration(injector.getInstance(UnitConversionService.class).convert(spentTime.getUnit(), timesheetLine.getTimesheet().getUnit(), spentTime.getDuration()));
		
		return timesheetLine;
		
	}
	
	
	@Transactional(rollbackOn = {AxelorException.class, Exception.class})
	public void validate(Timesheet timesheet)  {
		
		List<Task> taskList = Lists.newArrayList();
		
		if(timesheet.getTimesheetLineList() != null)  {
			for(TimesheetLine timesheetLine : timesheet.getTimesheetLineList())  {
				
				Task task = timesheetLine.getTask();
				if(task != null && timesheetLine.getSpentTime() == null)  {
					timesheetLine.setSpentTime(
							injector.getInstance(SpentTimeService.class).createSpentTime(timesheetLine));
					if(!taskList.contains(task))  {
						taskList.add(task);
					}
				}
			}
		}
		
		save(timesheet);

		if(!taskList.isEmpty())  {
			injector.getInstance(TaskService.class).updateTaskProgress(taskList);
		}
	}
	
	
}
