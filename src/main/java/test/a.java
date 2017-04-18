package test;

public class a {/*

{'table' : 'customer', 'database' : 'postgreSQL', 'knows' :[{'table : 'store', 'foreignkey' : 'customer.store_id'},{'table : 'address', 'foreignkey' : 'customer.address_id'}], 'members':['customer.customer_id', 'customer.first_name', 'customer.last_name', 'customer.address_id'], 'query' : 'SELECT * FROM customer WHERE 1=1', 'priority' : 2.0}
{'table' : 'address', 'database' : 'postgreSQL', 'knows' :[{'table' : 'city', 'foreignkey' : 'address.city_id'}], 'members':['address.address_id', 'address.address', 'address.address2', 'address.district', 'address.city_id', 'address.postal_code', 'address.phone'], 'query' : 'SELECT * FROM address WHERE 1=1' , 'priority' : 2.1}	
{'table' : 'city', 'database' : 'postgreSQL', 'knows' :[{'table' : 'country', 'foreignkey' : 'city.country_id'}], 'members':['city.city_id', 'city.city', 'city.country_id'], 'query' : 'SELECT * FROM city WHERE 1=1', 'priority' : 2.2}	
{'table' : 'country', 'database' : 'postgreSQL', 'knows' :[{'table' : 'nessuno', 'foreignkey' : 'blebleble'}], 'members':['country.country_id', 'country.country'], 'query' : 'SELECT * FROM country WHERE 1=1', 'priority' : 2.3}	
{'table' : 'store', 'database' : 'neo4j', 'knows' :[{'table': 'staff', 'foreignkey' : 'store.manager_staff_id'},{'table': 'address', 'foreignkey' : 'store.address_id'}], 'members':['store.store_id', 'store.manager_staff_id', 'store.address_id'], 'query' : 'MATCH (store:store) WHERE 1=1', 'priority' : 1.2}
{'table' : 'rental', 'database' : 'neo4j', 'knows' :[{'table':'customer', 'foreignkey' : 'rental.customer_id'},{'table':'staff', 'foreignkey' : 'rental.staff_id'},{'table':'inventory', 'foreignkey' : 'rental.inventory_id'}], 'members':['rental.inventory_id', 'rental.staff_id', 'rental.rental_date', 'rental.customer_id', 'rental.rental_id'], 'query' : 'MATCH (rental:rental) WHERE 1=1' , 'priority' : 1.0}	
{'table' : 'payment', 'database' : 'neo4j', 'knows' :[{'table':'customer', 'foreignkey' : 'payment.customer_id'},{'table':'staff', 'foreignkey' : 'payment.staff_id'},{'table':'rental', 'foreignkey' : 'payment.rental_id'}], 'members':['payment.amount', 'payment.payment_id', 'payment.staff_id', 'payment.customer_id', 'payment.payment_date', 'payment.rental_id'], 'query' : 'MATCH (payment:payment) WHERE 1=1' , 'priority' : 1.2}
{'table' : 'staff', 'database' : 'neo4j', 'knows' :[{'table': 'address', 'foreignkey' : 'staff.address_id'},{'table': 'store', 'foreignkey' : 'staff.store_id'}], 'members':['staff.store_id', 'staff.staff_id', 'staff.address_id', 'staff.last_name', 'staff.active', 'staff.first_name', 'staff.email'], 'query' : 'MATCH (staff:staff) WHERE 1=1' , 'priority' : 1.3}
	
	


*/
}
