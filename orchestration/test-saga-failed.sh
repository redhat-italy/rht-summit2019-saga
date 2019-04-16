########################### Test Saga
echo -e "\nStart Test Saga"
echo -e "\nBuying Ticket"

orderId=135
curl -X POST -H "Accept:application/json" -H "Content-Type:application/json" -d '{"orderId":"'$orderId'", "accountId": "AA2", "name": "Eminem - Atlanta 20th September 2019", "numberOfPersons": "1", "totalCost": 90}' http://booking-client-lra-thorntail-summit-saga.$(minishift ip).nip.io/book

echo -e "Order Id: " + $orderId
echo -e "\nTicket Refused"

echo -e "\nGet Ticket"
ticket=$(curl  http://ticket-lra-thorntail-summit-saga.$(minishift ip).nip.io/tickets/orderId/$orderId)
echo -e "Ticket: " + $ticket

sleep 5

echo -e "\nGet Insurance"
insurance=$(curl  http://insurance-lra-thorntail-summit-saga.$(minishift ip).nip.io/insurances/orderId/$orderId)
echo -e "Insurance Refused: " + $insurance

sleep 5

echo -e "\nGet Payment"
payment=$(curl  http://payment-lra-thorntail-summit-saga.$(minishift ip).nip.io/payments/orderId/$orderId)
echo -e "Payment Refused: " + $payment

sleep 5

echo -e "\nEnd Test Saga"