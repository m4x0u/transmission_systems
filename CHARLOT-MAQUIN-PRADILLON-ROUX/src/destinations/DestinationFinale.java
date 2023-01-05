package destinations;

import information.Information;
import information.InformationNonConformeException;

public class DestinationFinale extends Destination<Boolean> {

	/**
	 * Permet de recevoir une information.
	 * 
	 * @param information - L'information a  recevoir
	 */
	@Override
	public void recevoir(Information<Boolean> information) throws InformationNonConformeException {
		this.informationRecue = information;
	}
}
