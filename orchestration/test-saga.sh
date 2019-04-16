########################### Test Saga
echo -e "\nStart Test Saga"
echo -e "\nBuying Ticket"
orderId=$(curl -X POST -H "Accept:application/json" -H "Content-Type:application/json" -d '{"orderId":"131", "accountId": "AA2", "name": "Lady Gaga - NYC 18 june 2019", "numberOfPersons": "1", "totalCost": 60}' http://booking-client-lra-thorntail-summit-saga.$(minishift ip).nip.io/book)

echo -e "Order Id: " + $orderId
echo -e "\nTicket bought"

echo -e "\nGet Ticket"
ticket=$(curl  http://ticket-lra-thorntail-summit-saga.$(minishift ip).nip.io/tickets/orderId/$orderId)
echo -e "Ticket: " + $ticket

sleep 5

echo -e "\nGet Insurance"
insurance=$(curl  http://insurance-lra-thorntail-summit-saga.$(minishift ip).nip.io/insurances/orderId/$orderId)
echo -e "Insurance: " + $insurance

sleep 5

echo -e "\nGet Payment"
payment=$(curl  http://payment-lra-thorntail-summit-saga.$(minishift ip).nip.io/payments/orderId/$orderId)
echo -e "Payment: " + $payment

sleep 5

echo -e "\nEnd Test Saga"
