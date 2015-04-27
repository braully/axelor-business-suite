package com.axelor.apps.hr.service.employee;

import java.math.BigDecimal;

import com.axelor.apps.base.service.user.UserService;

public interface EmployeeService extends UserService{
	
	public BigDecimal getDurationHours(BigDecimal userDuration);
		
	public BigDecimal getUserDuration(BigDecimal hourDuration);
	
}