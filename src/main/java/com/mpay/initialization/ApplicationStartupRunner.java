package com.mpay.initialization;

import com.mpay.enums.WalletType;
import com.mpay.model.Roles;
import com.mpay.model.UserProfile;
import com.mpay.model.Wallet;
import com.mpay.repository.WalletRepository;
import com.mpay.service.RoleService;
import com.mpay.service.UserProfileService;
import com.mpay.util.Constants;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.Optional;

@Component
@Slf4j
public class ApplicationStartupRunner implements CommandLineRunner {

    @Autowired
    UserProfileService userProfileService;

    @Autowired
    RoleService roleService;

    @Autowired
    PasswordEncoder passwordEncoder;
    
    @Autowired
    WalletRepository walletRepository;

    @Override
    public void run(String... args) throws Exception {
        // Create admin User if not exist
        UserProfile admin = userProfileService.findByUserName("admin@jawk.com");
        if (admin == null) {
            // Create admin role if not exist
            Roles role = roleService.findByName(Constants.SUPER_ADMIN);
            if (role == null) {
                role = new Roles();
                role.setName(Constants.SUPER_ADMIN);
                role.setRoleFullName(Constants.SUPER_ADMIN);
                roleService.saveOrUpdateRole(role);
            }

            admin = new UserProfile();
            admin.setUsername("admin@jawk.com");
            admin.setEmail("admin@jawk.com");
            admin.setFirstName("Admin");
            admin.setLastName("JAWK");
            admin.setPassword(passwordEncoder.encode("jawk@admin2023"));
            admin.setIsUserActive(true);
            admin.setRole(role);
            admin.setDateCreated(new Timestamp(System.currentTimeMillis()));
            admin.setDateModified(new Timestamp(System.currentTimeMillis()));
            admin.setCreatedBy(1L);
            admin.setModifiedBy(1L);
            userProfileService.saveOrUpdateUser(admin);
        }

		Optional<Wallet> checkWalletForSystem = walletRepository.getWalletForSystem(WalletType.PAYMENT_SYSTEM);
		if (!checkWalletForSystem.isPresent()) {
			Wallet wallet = new Wallet();
			wallet.setBalanceAmount(BigDecimal.valueOf(0));
			wallet.setCommissionAmount(BigDecimal.valueOf(0));
			wallet.setWalletType(WalletType.PAYMENT_SYSTEM);
			walletRepository.save(wallet);
			log.debug("Wallet created for system with wallet type : " + WalletType.PAYMENT_SYSTEM.toString());
		}
    }

}
