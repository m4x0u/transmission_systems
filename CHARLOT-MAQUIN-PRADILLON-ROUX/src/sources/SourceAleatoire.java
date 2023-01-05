package sources;

import java.util.Random;

import information.Information;

/**
 * Une source qui envoie un message aleatoire
 */
public class SourceAleatoire extends Source<Boolean> {

	/**
	 * Constructeur d'une source aleatoire.
	 * Elle envoie un message de longueur 7 en booléens.
	 */
    public SourceAleatoire () {
        informationGeneree = new Information<Boolean>();
        Random random = new Random();
        for (int i = 0; i < 7; i++) {
        	getInformationGeneree().add(random.nextBoolean());
        }
    }

    /**
     * Constructeur d'une source aléatoire personnalisable.
     * Elle envoie un message de longueur personnalisée en booléens.
     * 0 et 1 sont équiprobables.
     * 
     * @param l - Longueur du message binaire à envoyer
     */
    public SourceAleatoire (int l) {
        informationGeneree = new Information<Boolean>();
        Random random = new Random();
        for (int i = 0; i < l; i++) {
        	getInformationGeneree().add(random.nextBoolean());
        }
    }

    /**
     * Constructeur d'une source aléatoire personnalisable supportant une graine de génération.
     * Elle envoie un message de longueur personnalisée à partir d'une graine personnalisée en booléens.
     * C'est la graine qui définit les probabilités des symboles.
     * 
     * @param l - Longueur du message binaine à envoyer
     * @param seed - Graine pour l'aleatoire
     */
    public SourceAleatoire (int l, int seed) {
        informationGeneree = new Information<Boolean>();
        Random random = new Random(seed);
        for (int i = 0; i < l; i++) {
        	getInformationGeneree().add(random.nextBoolean());
        }
    }
    
    public Information<Boolean> getInformationGeneree(){
    	return informationGeneree;
    }

}
