package com.mpay.service;

import com.mpay.dto.MerchantBankAccountRequest;
import com.mpay.dto.UPIDetailsDTO;
import com.mpay.dto.UserRequest;
import com.mpay.model.Merchant;
import com.mpay.model.MerchantBankAccount;
import com.mpay.model.Roles;
import com.mpay.model.UPIDetail;
import com.mpay.model.UserProfile;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;

@Service
public class CommonService {
    public void setMerchantBankAccountData(MerchantBankAccountRequest request, MerchantBankAccount merchantBankAccount) {
        request.setPaymentMethod(merchantBankAccount.getPaymentMethod());
        request.setBankName(merchantBankAccount.getBankName());
        request.setId(merchantBankAccount.getMerchantBankAccountId());
        Merchant merchant = merchantBankAccount.getMerchant();
        request.setMerchantName(merchant.getName());
        request.setMerchantId(merchant.getMerchantId());
        request.setAccountName(merchantBankAccount.getAccountName());
        request.setAccountNumber(merchantBankAccount.getAccountNumber());
        request.setApprovalStatus(merchantBankAccount.getApprovalStatus());
        request.setUpiId(merchantBankAccount.getUpiId());
        request.setContactNumber(merchantBankAccount.getContactNumber());
        request.setEmailAddress(merchantBankAccount.getEmailAddress());
        request.setIfscCode(merchantBankAccount.getIfscCode());
        request.setDateCreated(merchantBankAccount.getDateCreated().getTime());
    }

    public void setUserDetails(UserRequest request, UserProfile user, boolean list) {
        if (!list) {
            List<UPIDetail> upiList = user.getUPIDetails();
            List<UPIDetailsDTO> upiDTOList = new ArrayList<>();
            if(upiList != null) {
                for (UPIDetail upiDetail : upiList) {
                    UPIDetailsDTO upiDetailsDTO = new UPIDetailsDTO();
                    setUPIDetails(upiDetailsDTO, upiDetail);
                    upiDTOList.add(upiDetailsDTO);
                }
            }
            request.setUpiInfo(upiDTOList);
        }
        request.setMemberAcceptOrder(user.getMemberAcceptOrder());
        request.setFirstName(user.getFirstName());
        request.setLastName(user.getLastName());
        request.setFullName(user.getFirstName() + " " + user.getLastName());
        request.setEmailAddress(user.getEmail());
        request.setContactNumber(user.getContactNumber());
        request.setGender(user.getGender());
        request.setId(user.getUserId());
        if (!list) {
            request.setDailyPayoutLimitAmount(user.getDailyPayoutLimitAmount());
            request.setSinglePayoutMinAmount(user.getSinglePayoutMinAmount());
            request.setSinglePayoutMaxAmount(user.getSinglePayoutMaxAmount());
            Roles role = user.getRole();
            if (role != null) {
                request.setRole(role.getName());
                request.setRoleId(role.getRoleId());
            }
            Merchant merchant = user.getMerchant();
            if (merchant != null) {
                request.setMerchantId(merchant.getMerchantId());
                request.setMerchantName(merchant.getName());
            }
        }
    }

    public static void setUPIDetails(UPIDetailsDTO request, UPIDetail upiDetail) {
        request.setId(upiDetail.getUpiDetailsId());
        request.setPaymentType(upiDetail.getPaymentType().toString());
        request.setAccountName(upiDetail.getAccountName());
        request.setMobileNumber(upiDetail.getMobileNumber());
        request.setDefaultPayment(upiDetail.getIsDefault());

        request.setUpiId(upiDetail.getUpi());
        if (upiDetail.getUpiScanner() != null) {
            String scannerImg = Base64.getEncoder().encodeToString(upiDetail.getUpiScanner());
            request.setScannerImg(scannerImg);
        }
        request.setUpiType(upiDetail.getUpiType());

        request.setBankName(upiDetail.getBankName());
        request.setAccountNumber(upiDetail.getAccountNumber());
        request.setEwallet(upiDetail.getEwallet());
        request.setIfscCode(upiDetail.getIfscCode());

        request.setEwallet(upiDetail.getEwallet());
    }

    public void setUPIDetailsForPayInOrder(HashMap<String, Object> map, UPIDetail upiDetail) {
        map.put("paymentType", upiDetail.getPaymentType().toString());
        map.put("accountName", upiDetail.getAccountName());
        map.put("mobileNumber", upiDetail.getMobileNumber());

        if (StringUtils.equalsIgnoreCase(upiDetail.getPaymentType().toString(), "UPI")) {
            map.put("UPI", upiDetail.getUpi());
            if (upiDetail.getUpiScanner() != null) {
                String scannerImg = Base64.getEncoder().encodeToString(upiDetail.getUpiScanner());
                map.put("scannerImg", scannerImg);
            }
            map.put("upiType", upiDetail.getUpiType());
        } else if (StringUtils.equalsIgnoreCase(upiDetail.getPaymentType().toString(), "NETBANKING")) {
            map.put("bankName", upiDetail.getBankName());
            map.put("accountNumber", upiDetail.getAccountNumber());
            map.put("ifscCode", upiDetail.getIfscCode());
        } else if (StringUtils.equalsIgnoreCase(upiDetail.getPaymentType().toString(), "EWALLET")) {
            map.put("ewallet", upiDetail.getEwallet());
        }
    }
}
