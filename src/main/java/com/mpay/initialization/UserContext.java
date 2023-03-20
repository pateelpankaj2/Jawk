package com.mpay.initialization;

import com.mpay.exceptions.AccessControlException;
import com.mpay.model.Merchant;
import com.mpay.model.UserProfile;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class UserContext {
    private static Integer USER_PRIVILEGE_ID;

    public static UserProfile getUserContext() {
        UserProfile userProfile = null;
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            return null;
        }
        if (!"anonymousUser".equalsIgnoreCase(authentication.getPrincipal().toString())) {
            userProfile = (UserProfile) authentication.getPrincipal();
        }
        return userProfile;
    }

    public static Integer get() {
        UserProfile userProfile = getUserContext();
        if (userProfile != null) {
            USER_PRIVILEGE_ID = Math.toIntExact(userProfile.getRole().getRoleId());
        }
        return USER_PRIVILEGE_ID;
    }

    public static UserProfile getUser() {
        return getUserContext();
    }

    public static String getPrivilege(int privilegeId) {
        return PrivilegeIDs.toString(privilegeId);
    }

    private static boolean isAdmin() {
        return PrivilegeIDs.SUPER_ADMIN == get();
    }

    public static void assertIsAdmin() throws AccessControlException {
        if (!isAdmin()) {
            throw new AccessControlException(("Access denied - requires privilege: ADMIN, current privilege: " + getPrivilege(USER_PRIVILEGE_ID)));
        }
    }

    public static void assertIsUser() throws AccessControlException {
        if (PrivilegeIDs.MERCHANT_ADMIN != get() && PrivilegeIDs.USER != get() && PrivilegeIDs.MEMBER != get()&& !isAdmin()) {
            throw new AccessControlException(("Access denied - requires privilege: USER, current privilege: " + getPrivilege(USER_PRIVILEGE_ID)));
        }
    }

    public static void assertIsMerchant(Long merchantId) throws AccessControlException {
        if (PrivilegeIDs.MERCHANT_ADMIN != get() && !isAdmin())
            throw new AccessControlException(("Access denied - requires privilege: MERCHANT, current privilege: " + getPrivilege(USER_PRIVILEGE_ID)));

        if (!isAdmin() && merchantId != getUserMerchantId()) {
            throw new AccessControlException(("Access denied - Merchant mismatch, current merchant: " + getUserMerchantId()));
        }
    }

    public static void assertIsMember() throws AccessControlException {
        if (PrivilegeIDs.MEMBER != get() && !isAdmin())
            throw new AccessControlException(("Access denied - requires privilege: MEMBER, current privilege: " + getPrivilege(USER_PRIVILEGE_ID)));
    }

    public static Long getUserId() {
        UserProfile userProfile = getUserContext();
        return userProfile != null ? userProfile.getUserId() : null;
    }
    public static Long getUserMerchantId() {
        UserProfile userProfile = getUserContext();
        Merchant merchant = userProfile != null ? userProfile.getMerchant() : null;
        return merchant != null ? merchant.getMerchantId() : null;
    }

}
