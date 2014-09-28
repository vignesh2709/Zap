//=============================================================================
// Gathers data from Zappos API and uses it to find gift combinations.
//
// VIGNESH NARAYANAN
//=============================================================================
package vignesh_zappos_challenge_solution;

import java.io.*;
import java.util.*;
import org.json.simple.*;
import org.json.simple.parser.*;

public class GiftSearcher {
	private int numItems;			//number of items
	private double totalPrice;		//total price of items
	private double maxPrice;		//max feasible price
	private int page;				//page of results
	private JSONArray products;		//products in range
	private ArrayList<Product> productObjects; //JSON->Product list
	private ArrayList<ProductCombo> productCombos; //list of product combos
	private final double TOL = Math.pow(10, -7);  //tolerance for subtracting doubles
	private final int MAXCOMBOS = 30;
	
	/**
	 * Constructs a new GiftSearcher
	 * @param num The number of items to search for
	 * @param total The maximum total price of combined items
	 */
	public GiftSearcher(int num, double total) {
		numItems = num;
		totalPrice = total;
		maxPrice = Integer.MAX_VALUE; 	//will set later
		page = 1;					//will pull at least one page of results
		products = new JSONArray();
		productObjects = new ArrayList<Product>();
		productCombos = new ArrayList<ProductCombo>();
	}
	
	/**
	 * Extracts the price as a double from a JSON product object
	 * @param item The JSON Object product
	 * @return The price as a double
	 */
	private Double getPrice(Object item){
		return Double.parseDouble(((String) ((JSONObject) item).get("price")).substring(1));
	}
	
	/**
	 * Gets all products that are within the feasible price range, given the  
	 * number of items and the total price.
	 * Only gets products up to (total - (numItems-1)*(min price)), since that's
	 * the maximum amount.
	 * @return The JSON array of objects in String format, or "" if (numItems)*(min price)
	 * is greater than the total price
	 * @throws IOException 
	 * @throws ParseException
	 */
	@SuppressWarnings("unchecked")
	private void setProductsInRange() throws IOException, ParseException {
		//get maximum amount of products (100), starting at lowest price, and pull out results
		String reply = Parsing.httpGet(Parsing.BASEURL + "&term=&limit=100&sort={\"price\":\"asc\"}");
		JSONObject replyObject = Parsing.parseReply(reply);
		JSONArray resultArray = Parsing.getResults(replyObject);
		
		//get the first product's price (substring(1) to skip the $)
		double firstPrice = getPrice(resultArray.get(0));
		
		//if cheapest n items still over total price, then return empty string
		if( (firstPrice * numItems) > totalPrice) {
			products = null;
		}
		
		//otherwise, figure out what maximum price is given the minimum price
		maxPrice = totalPrice - (numItems - 1)*(firstPrice);
		
		//increment page, since we've already pulled the first page of results
		page++;
		
		//get the last product's price (substring(1) to skip the $)
		Double lastPrice = getPrice(resultArray.get(resultArray.size() - 1));
		
		//while the last price in returned page of results is less than max price,
		//pull another page of results
		while(lastPrice < maxPrice) { 
			//System.out.println("Last price: " + lastPrice);
			String nextPage = Parsing.httpGet(Parsing.BASEURL + "&term=&limit=100&sort={\"price\":\"asc\"}&page=" + page);
			//System.out.println("Pulling page " + page + "...");
			JSONObject nextObject = Parsing.parseReply(nextPage);
			JSONArray nextArray = Parsing.getResults(nextObject);
			
			//append new page of results to original array
			resultArray.addAll(nextArray);
			
			//get new last product and price
			lastPrice = getPrice(nextArray.get(nextArray.size() - 1));
			
			page++;
		}

		//return resultArray.toString();
		products = resultArray;
	}
	
	/**
	 * Converts JSONObjects into Products, puts products in price range in
	 * ArrayList to be sorted and searched
	 */
	private void setSearchableProducts() {
		//add the first (smallest price) object
		productObjects.add(new Product((JSONObject)products.get(0)));
		
		//count how many times a price has already shown up
		int already = 1;
		int numPrices = 1;
		//go through the whole 
		for(int i = 1; i < products.size() && getPrice(products.get(i)) < maxPrice; i++) {
			double currentPrice = getPrice(products.get(i));
			if( currentPrice > productObjects.get(numPrices-1).getPrice()) {
				productObjects.add(new Product((JSONObject)products.get(i)));
				numPrices++;
				already = 1;
			} else if(Math.abs(currentPrice - productObjects.get(numPrices-1).getPrice()) < TOL && already < numItems){
				productObjects.add(new Product((JSONObject)products.get(i)));
				numPrices++;
				already++;
			} else {
				while(i < products.size() && Math.abs(currentPrice - productObjects.get(numPrices-1).getPrice()) < TOL) {
					i++;
					currentPrice = getPrice(products.get(i));
				}
				i++;
				already = 0;
			}
		}
	}

	/**
	 * Recursively finds the product combinations of numItems items within $1 of the totalPrice
	 */
	private void setProductCombos() {
		setProductCombosRecursive(productObjects, totalPrice, new ArrayList<Product>());
	}
	
	/**
	 * Finds the product combinations of numItems items with $1 of the totalPrice through recursion and backtracking
	 * 
	 * REFERENCE: http://stackoverflow.com/questions/4632322/finding-all-possible-combinations-of-numbers-to-reach-a-given-sum
	 * @param productList The list of products to search
	 * @param target The target to get near
	 * @param partial The list of prices so far
	 */
	private void setProductCombosRecursive(ArrayList<Product> productList, double target, ArrayList<Product> partial) {
		int priceWithinAmount = 1;
		
		//if partial size > numItems, you already have too many items, so stop
		if(partial.size() > numItems) { return; }
		
		double sum = 0;
		for(Product x : partial) sum += x.getPrice();
		
		//if sum is within $1 of target, and partial size is numItems, and you don't already have too many product 
		//combos, then add another product combo
		if(Math.abs(sum - target) < priceWithinAmount && partial.size() == numItems && productCombos.size() < MAXCOMBOS) {
			//if no price combos yet, just add it on
			if(productCombos.size() == 0) {	productCombos.add(new ProductCombo(partial, totalPrice)); }
			//otherwise, check it against the most recent product combo to make sure you're not repeating
			//TODO: check all product combos
			else{
				ProductCombo testerCombo = productCombos.get(productCombos.size() -1);
				ProductCombo partialCombo = new ProductCombo(partial, totalPrice);
				if(!partialCombo.equals(testerCombo)) {
					productCombos.add(partialCombo);
				}
			}
		}
		//if sum is at or within $1 of target, then stop - done!
		if(sum >= target + priceWithinAmount) {
			return;
		}
		
		//otherwise, recursively continue adding another product to combo and test it
		for(int i = 0; i < productList.size() && !(partial.size() == numItems && sum < target); i++){
			ArrayList<Product> remaining = new ArrayList<Product>();
			Product n = productList.get(i);
			for(int j=i+1; j < productList.size(); j++) {remaining.add(productList.get(j)); }
			ArrayList<Product> partial_rec = new ArrayList<Product>(partial);
			partial_rec.add(n);
			setProductCombosRecursive(remaining, target, partial_rec);
		}
	}
	
	/**
	 * Sorts product combinations from closest to totalPrice to furthest away
	 */
	@SuppressWarnings("unchecked")
	private void sortProductCombos() {
		Collections.sort(productCombos);
	}
	
	/**
	 * Returns the gift combinations that are closest to the total dollar amount
	 * @throws IOException
	 * @throws ParseException
	 */
	public String getGiftCombos() throws IOException, ParseException {
		//get products from API
		System.out.println("Searching Zappos...");
		this.setProductsInRange();
		
		System.out.println("Finding combinations that work for you...");
		//convert to Products
		this.setSearchableProducts();
		//find combinations that work
		this.setProductCombos();
		//sort combos by how close they are to given total
		this.sortProductCombos();
		
		//see if you have any combos
		if(productCombos.size() != 0) {
			String toPrint = "\nDone!\n";
			for(ProductCombo x:productCombos) {
				toPrint += x.toString() + "\n";
			}
			return toPrint;
		}
		else {
			return "We couldn't find a set of items matching your criteria. " +
					"Please try again with fewer items or a larger dollar amount.";
		}
	}
	
}