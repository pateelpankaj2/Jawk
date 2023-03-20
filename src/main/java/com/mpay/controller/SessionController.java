package com.mpay.controller;

import com.mpay.dto.SessionResource;
import com.mpay.initialization.UserContext;
import com.mpay.model.Merchant;
import com.mpay.model.Roles;
import com.mpay.model.UserProfile;
import com.mpay.repository.MerchantRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import javax.persistence.EntityNotFoundException;
import javax.servlet.http.HttpServletRequest;
import java.text.ParseException;
import java.util.Optional;

@RestController
@RequestMapping(value = "/api/session", produces = MediaType.APPLICATION_JSON_VALUE)
public class SessionController {

	@Autowired
	MerchantRepository merchantRepository;

    /**
     * GET - Retrieves the web session
     *
     * @param request http request
     * @return EntityNotFoundException exception thrown if care plan, doctor, or patient is not found
     */
    @GetMapping("/web")
    public ResponseEntity<?> getWebSession(HttpServletRequest request) throws EntityNotFoundException, ParseException {

        UserProfile user = UserContext.getUser();
        if (user == null) {
            return new ResponseEntity<String>("Invalid token", HttpStatus.UNAUTHORIZED);
        }

        SessionResource session = new SessionResource();
        session.setUserId(user.getUserId());
        session.setFirstName(user.getFirstName());
        session.setLastName(user.getLastName());
        session.setEmailAddress(user.getEmail());
        session.setContactNumber(user.getContactNumber());
        Roles role = user.getRole();
        session.setRoleId(role.getRoleId());
        session.setRoleName(role.getName());
        Merchant merchant = user.getMerchant();
        if (merchant != null) {
            session.setMerchantId(merchant.getMerchantId());
            Optional<Merchant> checkMerchant = merchantRepository.findById(merchant.getMerchantId());
			if (checkMerchant.isPresent()) {
				session.setMerchantName(checkMerchant.get().getName());
			}
        }
        return new ResponseEntity<Object>(session, HttpStatus.OK);
    }
}