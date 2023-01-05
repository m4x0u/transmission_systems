package sources;

import java.util.LinkedList;

import information.Information;
import simulateur.ArgumentsException;

/**
 * Une source qui envoie toujours le meme message
 */
public class SourceFixe extends Source<Boolean> {

	/**
	 * Constructeur d'une source fixe predeterminee.
	 * Elle envoie '101101' en booleens.
	 */
    public SourceFixe () {
        informationGeneree = new Information<Boolean>();
        getInformationGeneree().add(true);
        getInformationGeneree().add(false);
        getInformationGeneree().add(true);
        getInformationGeneree().add(true);
        getInformationGeneree().add(false);
        getInformationGeneree().add(true);
    }

    /**
     * Constructeur d'une source fixe personnalisable.
     * Elle envoie un message personnalise en booleens.
     * 
     * @param m - Message binaire a envoyer (doit etre une chaine de 0 et de 1)
     * 
     * @throws ArgumentsException - Lorsque la chaine est non instanciee, vide ou contient autre chose que des 0 et des 1
     */
    public SourceFixe (String m) throws ArgumentsException {
    	if (m == null) {
    		throw new ArgumentsException("Le parametre n'est pas instancie.");
    	} else if (!m.matches("^[01]+$")) {
    		throw new ArgumentsException("Le parametre est vide ou contient autre chose que les caracteres '0' et '1'.");
    	}
    	informationGeneree = new Information<Boolean>();
    	for (int i = 0; i < m.length(); i++) {
    		if (m.charAt(i) == '0') {
    			getInformationGeneree().add(false);
    		} else {
    			getInformationGeneree().add(true);
    		}
    	}
    	// Ajouter une Information<float>
    }
    
    public Information<Boolean> getInformationGeneree(){
    	return informationGeneree;
    }
}
