package transmetteurs;

import destinations.DestinationInterface;
import information.Information;
import information.InformationNonConformeException;

/**
 * Un transmetteur ideal pour des messages analogiques.
 * On s'attend a un taux d'erreur binaire nul venant de lui.
 * Cette classe sert typiquement a lier un emetteur et un recepteur analogiques pour des simulations ideales.
 * 
 * @author groupeA3
 * @date 2022/09/15
 */
public class TransmetteurAnalogique extends Transmetteur<Float, Float>{

    /**
     * Permet d'emettre une information.
     * Elle est envoyee a toutes les destinations connectees.
     * 
     * @throws InformationNonConformeException
     */
	@Override
	public void emettre() throws InformationNonConformeException {
		this.informationEmise = this.informationRecue;
		for (DestinationInterface<Float> destinationConnectee : destinationsConnectees) {
            destinationConnectee.recevoir(informationEmise);
         }
	}

	/**
	 * Permet de recevoir une information et de la transmettre.
	 * 
	 * @param information - Information a recevoir
	 * @throws InformationNonConformeException 
	 */
	@Override
	public void recevoir(Information<Float> information) throws InformationNonConformeException {
		this.informationRecue = information;
    	this.emettre();
	}
}
