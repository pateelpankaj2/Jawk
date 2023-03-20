package com.mpay.service;

import com.mpay.dto.RoleDTO;
import com.mpay.model.Roles;
import com.mpay.repository.RoleRepository;
import com.mpay.util.Constants;

import java.util.ArrayList;
import java.util.List;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@Transactional
public class RoleService {

    @Autowired
    RoleRepository rolesRepository;

    public void saveOrUpdateRole(Roles role) {
        rolesRepository.save(role);
    }

    public Roles findByName(String name) {
        return rolesRepository.findByName(name);
    }

	public List<RoleDTO> getAllRoles() {
		List<Roles> findAll = rolesRepository.findAll();
		List<RoleDTO> roleDTOs = new ArrayList<RoleDTO>();
		if (CollectionUtils.isNotEmpty(findAll)) {
			findAll.stream().forEach(dto -> {
				if (!Constants.MERCHANT_ADMIN.equalsIgnoreCase(dto.getRoleFullName())
						&& !Constants.MERCHANT_SUBACCOUNT.equalsIgnoreCase(dto.getRoleFullName())) {
					RoleDTO roleDTO = new RoleDTO();
					roleDTO.setRoleId(dto.getRoleId());
					roleDTO.setRole(dto.getRoleFullName());
					roleDTOs.add(roleDTO);
				}
			});
		}
		return roleDTOs;
	}
}
