package lateralpraxis.lpdnd.types;

public class CashDeposit {
	//Variables to get and set Details for CustomerPayment
	private String Id;
	private String CustomerName;
	private String Cheque;
	private String PaymentDate;
	private String Amount;  
	private String UniqueId;

	//Constructor for class
	public CashDeposit (String Id , String CustomerName,String PaymentDate, String Cheque,  String Amount, String UniqueId) {
		this.Id = Id;
		this.CustomerName = CustomerName;
		this.PaymentDate = PaymentDate;
		this.Cheque = Cheque;
		this.Amount = Amount;       
		this.UniqueId = UniqueId;
	}
	/*start of code to get and set Id*/
	public String getId () {
		return Id;
	}

	public void setId(String Id){
		this.Id = Id;
	}
	/*end of code to get and set Id*/

	/*start of code to get and set Customer Name*/
	public String getCustomerName () {
		return CustomerName;
	}

	public void setCustomerName(String CustomerName){
		this.CustomerName = CustomerName;
	}
	/*end of code to get and set Customer Name*/

	/*start of code to get and set Payment Date*/
	public String getPaymentDate () {
		return PaymentDate;
	}

	public void setPaymentDate(String PaymentDate){
		this.PaymentDate = PaymentDate;
	}
	/*end of code to get and set Payment Date*/

	/*start of code to get and set Amount*/
	public String getAmount () {
		return Amount;
	}

	public void setAmount(String Amount){
		this.Amount = Amount;
	}
	/*end of code to get and set Amount*/

	/*start of code to get and set Cheque*/
	public String getCheque () {
		return Cheque;
	}

	public void setCheque(String Cheque){
		this.Cheque = Cheque;
	}
	/*end of code to get and set Cheque*/

	/*start of code to get and set UniqueId*/
	public String getUniqueId () {
		return UniqueId;
	}

	public void setUniqueId(String UniqueId){
		this.UniqueId = UniqueId;
	}
	/*end of code to get and set UniqueId*/

}
