package transmetteurs;

import destinations.DestinationInterface;
import information.Information;
import information.InformationNonConformeException;

/**
 * Un decodeur de donnees booleennes.
 * Il sert a decoder un signal recu (typiquement par un Recepteur).
 * On decode le codage 3 bits standard (etpae 5).
 * 
 * @date 2022/10/04
 * @author groupeA3
 */
public class Decodeur extends Transmetteur<Boolean, Boolean> {

	/**
	 * Permet de recevoir une information et de la transmettre.
	 * 
	 * @param information - Information a recevoir
	 * @throws InformationNonConformeException 
	 */
    @Override
    public void recevoir(Information<Boolean> information) throws InformationNonConformeException {
    	this.informationRecue = information;
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
    	decoder();
    	for (DestinationInterface<Boolean> destination : this.destinationsConnectees) {
    		destination.recevoir(this.informationEmise);
    	}
    }

    public void decoder() throws InformationNonConformeException {
    	if (this.informationRecue.nbElements() % 3 != 0) {
    		throw new InformationNonConformeException("[DECODEUR] Longueur du message recu non multiple de 3");
    	}
    	informationEmise = new Information<Boolean>();
    	String sequence;
    	for (int i = 0; i < informationRecue.nbElements(); i += 3) {
    		sequence = "";
    		for (int j = 0; j < 3; j++) {
    			if (informationRecue.iemeElement(i + j))
    				sequence = sequence + "1";
    			else
    				sequence = sequence + "0";
    		}
    		switch (sequence) {
    			case "000":
					informationEmise.add(false);
					break;
    			case "001":
    				informationEmise.add(true);
    				break;
    			case "010":
    				informationEmise.add(false);
    				break;
    			case "011":
    				informationEmise.add(false);
    				break;
    			case "100":
    				informationEmise.add(true);
    				break;
    			case "101":
    				informationEmise.add(true);
    				break;
    			case "110":
    				informationEmise.add(false);
    				break;
    			case "111":
    				informationEmise.add(true);
    				break;
    		}
    	}
    }
}
