package transmetteurs;

import destinations.DestinationInterface;
import information.Information;
import information.InformationNonConformeException;



public class Codeur extends Transmetteur<Boolean, Boolean> {
	
	protected Information <Boolean>  informationGeneree;
	

	/**
	 * Permet de recevoir et de coder une information reçus et de la transmettre.
	 * 
	 * @param information - Information a recevoir
	 * @throws InformationNonConformeException 
	 */
    @Override
    public void recevoir(Information<Boolean> information) throws InformationNonConformeException {
    	this.informationRecue = information;
    	this.coder();
    	
    	
    	this.emettre();
    }

    /**
     * Permet d'emettre une information.
     * Elle est envoyee a toutes les destinations connectees.
     * 
     * @throws InformationNonConformeException
     */
    @Override
    public void emettre() throws InformationNonConformeException {
    	this.informationEmise = this.informationGeneree;
    	for (DestinationInterface<Boolean> destination : this.destinationsConnectees) {
    		destination.recevoir(this.informationEmise);
    	}
    }
    
    
    protected void coder () {
    	informationGeneree = new Information <>();
    	for (Boolean bit : informationRecue) {
    		if (bit == true) {
    			 informationGeneree.add(true);
    			 informationGeneree.add(false);
    			 informationGeneree.add(true);
    		}
    		else {
   			 	informationGeneree.add(false);
   			 	informationGeneree.add(true);
   			 	informationGeneree.add(false);
    		}
    	}  	
    }
    
    
}
