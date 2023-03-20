package com.mpay.controller;

import com.mpay.dto.OrderRequest;
import com.mpay.dto.WithdrawAndDepositOrdersRequest;
import com.mpay.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping(path = "/api/orders")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @GetMapping("/{id}")
    public ResponseEntity<?> getOrderDetails(@PathVariable Long id) {
        return new ResponseEntity<>(orderService.getOrderDetails(id), HttpStatus.OK);
    }

    @PostMapping("/pay-in-order")
    public ResponseEntity<?> createPayInOrder(@RequestBody OrderRequest orderRequest) {
        try {
            //Long orderMerchantId = orderRequest.getMerchantId();
            //UserContext.assertIsMerchant(orderMerchantId);
            Map<String, Object> resultMap = orderService.savePayInOrder(orderRequest);
            if (resultMap.containsKey("errorMessage")) {
                return new ResponseEntity<>(resultMap, HttpStatus.BAD_REQUEST);
            }
            return new ResponseEntity<>(resultMap, HttpStatus.CREATED);

        } catch (
                Exception e) { // TODO: 21-02-2023 : Remove Exception class & add AccessControlException after enabling 'assertIsMerchant'
            throw new RuntimeException(e);
        }
    }

    @PostMapping("/pay-out-order")
    public ResponseEntity<?> createPayOutOrder(@RequestBody OrderRequest orderRequest) {
        try {
            //Long orderMerchantId = orderRequest.getMerchantId();
            //UserContext.assertIsMerchant(orderMerchantId);
            Map<String, Object> resultMap = orderService.savePayOutOrder(orderRequest);
            if (resultMap.containsKey("errorMessage")) {
                return new ResponseEntity<>(resultMap, HttpStatus.BAD_REQUEST);
            }
            return new ResponseEntity<>(resultMap, HttpStatus.CREATED);
        } catch (
                Exception e) { // TODO: 21-02-2023 : Remove Exception class & add AccessControlException after enabling 'assertIsMerchant'
            throw new RuntimeException(e);
        }
    }

    @PostMapping("/withdraw-order")
    private ResponseEntity<?> createWithdrawOrder(@RequestBody OrderRequest orderRequest) {
        Map<String, Object> resultMap = orderService.createWithdrawOrDepositOrder(orderRequest, "withdraw");
        if (resultMap.containsKey("errorMessage")) {
            return new ResponseEntity<>(resultMap, HttpStatus.BAD_REQUEST);
        }
        return new ResponseEntity<>(resultMap, HttpStatus.OK);
    }

    @PostMapping("/deposit-order")
    private ResponseEntity<?> createTopupOrder(@RequestBody OrderRequest orderRequest) {
        Map<String, Object> resultMap = orderService.createWithdrawOrDepositOrder(orderRequest, "deposit");
        if (resultMap.containsKey("errorMessage")) {
            return new ResponseEntity<>(resultMap, HttpStatus.BAD_REQUEST);
        }
        return new ResponseEntity<>(resultMap, HttpStatus.OK);
    }

    @PostMapping("/confirm-order-payment")
    public ResponseEntity<?> confirmOrderPayment(@RequestBody OrderRequest orderRequest) {
        Map<String, Object> result = orderService.confirmOrderPayment(orderRequest);
        if (result.containsKey("errorMessage")) {
            return new ResponseEntity<>(result, HttpStatus.EXPECTATION_FAILED);
        }
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @PostMapping("/reject-order")
    public ResponseEntity<?> rejectOrder(@RequestBody OrderRequest orderRequest) {
        Map<String, Object> result = orderService.rejectOrder(orderRequest);
        if (result.containsKey("errorMessage")) {
            return new ResponseEntity<>(result, HttpStatus.EXPECTATION_FAILED);
        }
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @PostMapping("/complete-order")
    public ResponseEntity<?> completeOrder(@RequestBody OrderRequest orderRequest) {
        Map<String, Object> completeOrder = orderService.completeOrder(orderRequest);
        if (completeOrder.containsKey("errorMessage")) {
            return new ResponseEntity<>(completeOrder, HttpStatus.EXPECTATION_FAILED);
        }
        return new ResponseEntity<>(completeOrder, HttpStatus.OK);
    }

    @PostMapping("/complete-withdraw-order")
    public ResponseEntity<?> completeWithdrawOrder(@RequestBody OrderRequest orderRequest) {
        Map<String, Object> completeOrder = orderService.completeWithdrawOrder(orderRequest);
        if (completeOrder.containsKey("errorMessage")) {
            return new ResponseEntity<>(completeOrder, HttpStatus.EXPECTATION_FAILED);
        }
        return new ResponseEntity<>(completeOrder, HttpStatus.OK);
    }

    @PostMapping("/complete-deposit-order")
    public ResponseEntity<?> completeDepositOrder(@RequestBody OrderRequest orderRequest) {
        Map<String, Object> completeOrder = orderService.completeDepositOrder(orderRequest);
        if (completeOrder.containsKey("errorMessage")) {
            return new ResponseEntity<>(completeOrder, HttpStatus.EXPECTATION_FAILED);
        }
        return new ResponseEntity<>(completeOrder, HttpStatus.OK);
    }

    @PostMapping("/assign-order")
    public ResponseEntity<?> assignOrder(@RequestBody OrderRequest orderRequest) {
        Map<String, Object> assignOrder = orderService.assignOrder(orderRequest);
        if (assignOrder.containsKey("errorMessage")) {
            return new ResponseEntity<>(assignOrder, HttpStatus.EXPECTATION_FAILED);
        }
        return new ResponseEntity<>(assignOrder, HttpStatus.OK);
    }

    // Admin order
	/*@PostMapping("/admin-order")
	public ResponseEntity<?> createAdminOrder(@RequestBody OrderRequest orderRequest) {
		Map<String, Object> resultMap = orderService.saveAdminOrder(orderRequest);
		if(resultMap.containsKey("errorMessage")) {
			return new ResponseEntity<>(resultMap, HttpStatus.BAD_REQUEST);
		}
		return new ResponseEntity<>(resultMap, HttpStatus.CREATED);
	}*/

    @GetMapping("/list")
    public ResponseEntity<?> getOrders(@RequestParam(name = "id", required = false) Long id, @RequestParam(name = "type", required = false) String type, @RequestParam(name = "status", required = false) String status, @RequestParam(name = "orderType", required = false) String orderType) {
        List<OrderRequest> orders = orderService.getOrders(id, type, status, orderType);
        return new ResponseEntity<>(orders, HttpStatus.OK);
	}

	@GetMapping("/order-analytics")
	private ResponseEntity<?> getOrderAnalytics(@RequestParam(name = "type") String type,
			@RequestParam(name = "id", required = false) Long id) {
		Map<String, Object> orderAnalytics = orderService.getOrderAnalytics(type, id);
		return new ResponseEntity<>(orderAnalytics, HttpStatus.OK);
	}

	@GetMapping("/open-orders")
	public ResponseEntity<?> getPendingOrders(@RequestParam("orderType") String orderType) {
		List<OrderRequest> openOrders = orderService.getOpenOrders(orderType);
		return new ResponseEntity<>(openOrders, HttpStatus.OK);
	}

	@GetMapping("/get-withdraw-orders")
	public ResponseEntity<?> getWithdrawOrders(@RequestParam("id") Long id, @RequestParam("type") String type) {
		List<WithdrawAndDepositOrdersRequest> withdrawOrders = orderService.getWithdrawOrders(id, type);
		return new ResponseEntity<>(withdrawOrders, HttpStatus.OK);
	}

	@GetMapping("/withdraw-order-analytics")
	private ResponseEntity<?> getWithdrawOrderAnalytics(@RequestParam(name = "id") Long id, @RequestParam(name = "type") String type) {
		Map<String, Object> orderAnalytics = orderService.getWithdrawOrderAnalytics(type, id);
		return new ResponseEntity<>(orderAnalytics, HttpStatus.OK);
	}

	@PutMapping("/update-order-amount")
	private ResponseEntity<?> updateOrderAmount(@RequestBody OrderRequest orderRequest) {
		Map<String, Object> resultMap = orderService.updateOrderAmount(orderRequest);
		if (resultMap.containsKey("errorMessage")) {
			return new ResponseEntity<>(resultMap, HttpStatus.BAD_REQUEST);
		}
		return new ResponseEntity<>(resultMap, HttpStatus.OK);
	}

	@GetMapping("/get-deposit-orders")
	public ResponseEntity<?> getDepositOrders(@RequestParam("id") Long id, @RequestParam("type") String type) {
		List<WithdrawAndDepositOrdersRequest> depositOrders = orderService.getDepositOrders(id, type);
		return new ResponseEntity<>(depositOrders, HttpStatus.OK);
	}

	@GetMapping("/member-profile")
	public ResponseEntity<?> getMemberProfileDetails(@RequestParam("id") Long id, @RequestParam("type") String type) {
		Map<String, Object> myOrders = orderService.getMemberProfileDetails(id, type);
		return new ResponseEntity<>(myOrders, HttpStatus.OK);
	}

	@GetMapping("/deposit-order-analytics")
	private ResponseEntity<?> getDepositOrderAnalytics(@RequestParam(name = "id") Long id, @RequestParam(name = "type") String type) {
		Map<String, Object> orderAnalytics = orderService.getDepositOrderAnalytics(type, id);
		return new ResponseEntity<>(orderAnalytics, HttpStatus.OK);
	}
}
