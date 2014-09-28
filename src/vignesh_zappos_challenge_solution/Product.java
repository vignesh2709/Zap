//=============================================================================
// This class represents a single Product
//
// VIGNESH NARAYANAN
//=============================================================================
package vignesh_zappos_challenge_solution;

import org.json.simple.*;

public class Product {
	private double price;		//product price
	private String id;			//product id
	private String name;		//name of product
	private String styleId;		//styleId of product
	private String priceString;	//String representation of price
	
	/**
	 * Constructs a Product from the JSONObject
	 * @param product The JSONObject provided
	 */
	public Product(JSONObject product) {
		//substring(1) on the price to remove the $
		price = Double.parseDouble(((String) product.get("price")).substring(1));
		id = (String)product.get("productId");
		name = (String)product.get("productName");
		styleId = (String)product.get("styleId");
		
		//format the price string with only 2 numbers after decimal place
		priceString = String.format("%.2f", price);
	}
	
	/**
	 * Returns String representation of product
	 * @return name, $price, (id: id, styleId: styleID)
	 */
	public String toString() {
		return name + ", $" + priceString + " (id:" + id + ", styleId:" + styleId + ")";
	}
	
	/**
	 * Returns the price of the Product
	 * @return
	 */
	public double getPrice() {
		return price;
	}
}
