package transmetteurs;

import destinations.DestinationInterface;
import information.Information;
import information.InformationNonConformeException;

/** 
 * Un transmetteur ideal de donnees booleennes.
 * On s'attend a un taux d'erreur binaire nul venant de lui.
 * Cette classe sert typiquement a lier une source binaire et une destination directement pour des simulations ideales.
 * 
 * @author groupeA3
 * @date 2022/09/08
 */
public class TransmetteurParfait extends Transmetteur<Boolean, Boolean> {

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
    	this.informationEmise = this.informationRecue;
    	for (DestinationInterface<Boolean> destination : this.destinationsConnectees) {
    		destination.recevoir(this.informationEmise);
    	}
    }
}
