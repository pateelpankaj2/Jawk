package com.mpay.service;

import com.mpay.enums.OrderStatus;
import com.mpay.model.Merchant;
import com.mpay.model.Order;
import com.mpay.repository.OrderRepository;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Credentials;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class WebhookService {

    @Autowired
    private OrderRepository orderRepository;

    @Async
    public void executeMerchantWebhook(long orderId) {

        Optional<Order> checkOrder = orderRepository.findById(orderId);

        if (checkOrder.isPresent()) {

            Order order = checkOrder.get();
            Merchant merchant = order.getMerchant();
            String webhookUrl = merchant.getWebhookURL();
            String username = merchant.getWebhookUsername();
            String password = merchant.getWebhookPassword();
            String orderStatus = order.getOrderStatus().toString();
            try {
                OkHttpClient client = new OkHttpClient.Builder().connectTimeout(30, TimeUnit.SECONDS)
                        .readTimeout(30, TimeUnit.SECONDS)
                        .build();

                FormBody formBody = new FormBody.Builder()
                        .add("orderNumber", order.getOrderNumber())
                        .add("orderStatus", orderStatus)
                        .build();
                if (StringUtils.equalsIgnoreCase(orderStatus, OrderStatus.REJECTED.toString())) {
                    formBody = new FormBody.Builder()
                            .add("orderNumber", order.getOrderNumber())
                            .add("orderStatus", orderStatus)
                            .add("rejectionComment", order.getRejectionComment())
                            .build();
                }

                Request request = new Request.Builder()
                        .url(webhookUrl)
                        .post(formBody)
                        .addHeader("Content-Type", "application/json")
                        .build();

                if (StringUtils.isNotBlank(username)) {
                    request = new Request.Builder()
                            .url(webhookUrl)
                            .post(formBody)
                            .addHeader("Content-Type", "application/json")
                            .addHeader("Authorization", Credentials.basic(username, password))
                            .build();
                }
                Response response = client.newCall(request).execute();
                String responseBody = response.body() != null ? response.body().string() : null;
                System.out.println(responseBody);
            } catch (Exception e) {
                log.error("Error while executing merchant webhook url. Merchant: {}, Order Number: {}, Error: {}", merchant.getMerchantId(), order.getOrderNumber(), e.getMessage());
            }
        }
    }
}
