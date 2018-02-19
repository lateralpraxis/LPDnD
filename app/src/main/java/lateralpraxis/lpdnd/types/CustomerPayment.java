package lateralpraxis.lpdnd.types;

public class CustomerPayment {
	//Variables to get and set Details for CustomerPayment
	private int Id;
    private String CompanyName;
    private String Amount;
    private String ChequeNumber;
    private String BankName;
    private String UniqueId;
    
  //Constructor for class
    public CustomerPayment (int Id , String CompanyName,String Amount, String ChequeNumber, String BankName, String UniqueId) {
        this.Id = Id;
        this.CompanyName = CompanyName;
        this.Amount = Amount;
        this.ChequeNumber = ChequeNumber;       
        this.BankName = BankName;
        this.UniqueId = UniqueId;
    }
    /*start of code to get and set Id*/
    public int getId () {
        return Id;
    }
    
    public void setId(int Id){
        this.Id = Id;
    }
    /*end of code to get and set Id*/
    
    /*start of code to get and set Company Name*/
    public String getCompanyName () {
        return CompanyName;
    }
    
    public void setCompanyName(String CompanyName){
        this.CompanyName = CompanyName;
    }
    /*end of code to get and set Company Name*/
    
    /*start of code to get and set Amount*/
    public String getAmount () {
        return Amount;
    }
    
    public void setAmount(String Amount){
        this.Amount = Amount;
    }
    /*end of code to get and set Amount*/
    
    /*start of code to get and set Cheque Number*/
    public String getChequeNumber () {
        return ChequeNumber;
    }
    
    public void setChequeNumber(String ChequeNumber){
        this.ChequeNumber = ChequeNumber;
    }
    /*end of code to get and set Cheque Number*/
    
    /*start of code to get and set Cheque Number*/
    public String getBankName () {
        return BankName;
    }
    
    public void setBankName(String BankName){
        this.BankName = BankName;
    }
    /*end of code to get and set Cheque Number*/
    
    /*start of code to get and set UniqueId*/
    public String getUniqueId () {
        return UniqueId;
    }
    
    public void setUniqueId(String UniqueId){
        this.UniqueId = UniqueId;
    }
    /*end of code to get and set UniqueId*/
    
}
