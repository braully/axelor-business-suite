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
package com.axelor.csv.script;

import com.axelor.inject.Beans;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.axelor.auth.db.Group;
import com.axelor.auth.db.Permission;
import com.axelor.auth.db.repo.PermissionRepository;
import com.axelor.auth.db.repo.GroupRepository;

import com.google.inject.persist.Transactional;


public class ImportPermission extends PermissionRepository {
		
		@Transactional
		public Object importPermission(Object bean, Map values) {
			assert bean instanceof Permission;
	        try{
	        	
	        	GroupRepository groupRepository = Beans.get(GroupRepository.class);
	        	
	            Permission permission = (Permission) bean;
				String groups = (String) values.get("group");
				if(permission.getId()!= null){
					if(groups != null && !groups.isEmpty()){
						for(Group group: groupRepository.all().filter("code in ?1",Arrays.asList(groups.split("\\|"))).fetch()){
							Set<Permission> permissions = group.getPermissions();
							if(permissions == null)
								permissions = new HashSet<Permission>();
							permissions.add(find(permission.getId()));
							group.setPermissions(permissions);
							groupRepository.save(group);
						}
					}
				}
				return permission;
	        }catch(Exception e){
	            e.printStackTrace();
	        }
			return bean;
		}
		
}



