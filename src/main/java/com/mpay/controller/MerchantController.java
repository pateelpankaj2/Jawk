package com.mpay.controller;

import java.util.List;
import java.util.Map;

import com.mpay.dto.MerchantSettlementRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.mpay.dto.MerchantBankAccountRequest;
import com.mpay.dto.MerchantRequest;
import com.mpay.repository.MerchantBankAccountRepository;
import com.mpay.service.MerchantService;

@RestController
@RequestMapping(value = "/api/merchants")
public class MerchantController {

	@Autowired
	MerchantService merchantService;

	@Autowired
	MerchantBankAccountRepository merchantBankAccountRepository;

	@PostMapping("/create")
	public ResponseEntity<?> CreateMerchant(@RequestBody MerchantRequest merchantRequest) {
		Map<String, Object> saveOrUpdateMerchant = merchantService.saveOrUpdateMerchant(merchantRequest, null);
		if (saveOrUpdateMerchant.containsKey("errorMessage")) {
			return new ResponseEntity<>(saveOrUpdateMerchant, HttpStatus.BAD_REQUEST);
		}
		return new ResponseEntity<>(saveOrUpdateMerchant, HttpStatus.OK);
	}

	@PutMapping("/update/{id}")
	public ResponseEntity<?> updateMerchant(@PathVariable("id") Long id, @RequestBody MerchantRequest merchantRequest) {
		Map<String, Object> saveOrUpdateMerchant = merchantService.saveOrUpdateMerchant(merchantRequest, id);
		if (saveOrUpdateMerchant.containsKey("errorMessage")) {
			return new ResponseEntity<>(saveOrUpdateMerchant, HttpStatus.BAD_REQUEST);
		}
		return new ResponseEntity<>(saveOrUpdateMerchant, HttpStatus.OK);
	}

	@DeleteMapping("/delete/{id}")
	public ResponseEntity<?> deleteMerchant(@PathVariable("id") Long id) {
		Boolean deleteMerchant = merchantService.deleteMerchant(id);
		if (deleteMerchant) {
			return new ResponseEntity<>(HttpStatus.OK);
		} else {
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}
	}

	@GetMapping("/all")
	public ResponseEntity<?> getAllMerchant() {
		List<MerchantRequest> allMarchart = merchantService.getAllMarchart();
		return new ResponseEntity<>(allMarchart, HttpStatus.OK);
	}

	@GetMapping("/members-upis/{id}")
	public ResponseEntity<?> getMerchantMembersUPIDetails(@PathVariable("id") Long id) {
		return new ResponseEntity<>(merchantService.getMerchantMemberUPIDetails(id), HttpStatus.OK);
	}

	@GetMapping("/settled-amounts")
	public ResponseEntity<?> settledAmounts(@RequestParam(name = "merchantId", required = false) Long merchantId) {
		return new ResponseEntity<>(merchantService.getMerchantSettledAmounts(merchantId), HttpStatus.OK);
	}

	@PostMapping("/settle-amount")
	public ResponseEntity<?> settleAmount(@RequestBody MerchantSettlementRequest request) {
		return new ResponseEntity<>(merchantService.settleAmount(request), HttpStatus.OK);
	}

	@PostMapping("/create-bank-account")
	public ResponseEntity<?> createMerchantBankAccount(
			@RequestBody MerchantBankAccountRequest merchantBankAccountRequest) {
		merchantService.createOrUpdateMerchantBankAccount(merchantBankAccountRequest);
		return new ResponseEntity<>(HttpStatus.OK);
	}

	@PutMapping("/update-bank-account")
	public ResponseEntity<?> updateMerchantBankAccount(
			@RequestBody MerchantBankAccountRequest merchantBankAccountRequest) {
		merchantService.createOrUpdateMerchantBankAccount(merchantBankAccountRequest);
		return new ResponseEntity<>(HttpStatus.OK);
	}

	@GetMapping("/bank-accounts/{id}")
	public ResponseEntity<?> getMerchantBankAccounts(@PathVariable("id") Long merchantId) {

		List<MerchantBankAccountRequest> merchantBankDetails = merchantService.getMerchantBankDetails(merchantId);
		return new ResponseEntity<>(merchantBankDetails, HttpStatus.OK);
	}

	@GetMapping("/get-bank-details/{id}")
	public ResponseEntity<?> getIdAndAccountNamesByMerchant(@PathVariable("id") Long merchantId) {

		List<Object> idAndAccountNamesByMerchantId = merchantService.getIdAndAccountNamesByMerchantId(merchantId);
		return new ResponseEntity<>(idAndAccountNamesByMerchantId, HttpStatus.OK);
	}
}
