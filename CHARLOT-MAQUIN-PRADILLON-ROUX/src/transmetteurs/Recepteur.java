package transmetteurs;

import java.util.Objects;

import destinations.DestinationInterface;
import information.Information;
import information.InformationNonConformeException;

/**
 * La classe Recepteur specialise la classe abstraite Transmetteur pour faire de la reception.
 * Un recepteur prend l'information transmise par un autre transmetteur et la transmet a une destination.
 * 
 * @author groupeA3
 * @date 2022/09/15
 */
public class Recepteur extends Transmetteur<Float, Boolean> {

	/** Information contenant l'information recue par le recepteur d'un transmetteur, une fois formatee/convertie au format voulu */
	private Information<Boolean> informationGeneree;

	/** Precise le format de reception de l'information voulu */ 
	private String forme;

	/** Nombre d'echantillons avec lequel proceder a l'echantillonnage du signal */
	private int nbEch;

	/** Valeur analogique minimale accessible par l'information (amplitude) */
	private float Amin;

	/** Valeur analogique maximale accessible par l'information (amplitude) */
	private float Amax;

	/**
	 * Le constructeur definit tous les attributs pour creer une nouvelle instance de la classe.
	 * 
	 * @param forme
	 * @param nbEch
	 * @param Amin
	 * @param Amax
	 */
	public Recepteur(String forme, int nbEch, float Amin, float Amax) {
		this.forme = forme;
		this.nbEch = nbEch;
		this.Amin = Amin;
		this.Amax = Amax;
	}

	/**
	 * Convertit l'information (analogique) recue en information (booleenne) generee selon le format voulu.
	 * Le format est defini par l'attribut forme.
	 * 
	 * Valeurs reconnues :
	 * - "RZ"
	 * - "NRZ"
	 * - "NRZT"
	 */
	public void convert() {
		informationGeneree = new Information<>();
		if (Objects.equals(forme,"RZ")) {
			convertFromRZ();
		} else if(Objects.equals(forme, "NRZ")) {
			convertFromNRZ();
		} else if(Objects.equals(forme,"NRZT")) {
			convertFromNRZT();
		}
	}

	/**
	 * Methode principale du recepteur.
	 * Elle sert a recevoir l'information d'un autre transmetteur, a la convertir au format voulu, puis a l'emettre vers les destinations connectees.
	 */
	@Override
	public void recevoir(Information<Float> information) throws InformationNonConformeException {
		informationRecue = information;
		convert();
		emettre();
	}

	/**
	 * Methode d'emission de l'information generee vers les destinations connectees.
	 * Elle intervient typiquement apres formatage.
	 */
	public void emettre() throws InformationNonConformeException {
		informationEmise = informationGeneree;
		for (DestinationInterface<Boolean> destinationConnectee : destinationsConnectees) {
			destinationConnectee.recevoir(informationEmise);
		}
	}
	
	
	/**
	 * Convertit l'information analogique RZ recue en booleen.
	 */
	protected void convertFromRZ() {
		// float valeurSymbole = 0.0f;
		int indice = 0;
		float center = (Amax+Amin)/2;
		float somme = 0;
		float moyenne = 0;
		float nbEchFloat = (float) nbEch;
		int nbEchUtils = 0;
		for (float value: informationRecue) {
			// float seuilTolerence = 0; // Le seuil de tolerence a definir pour plus tard;
			if (indice > nbEchFloat/3 && indice < 2 * nbEchFloat/3) {
				somme += value;
				nbEchUtils++;
			}
			indice++;
			
			/* 
			for (int i = 0; i < nbEch; i++) {
				
				// On cherche parmi les valeurs au milieu du symbole
				// si la moyenne est egale a Amax (sans seuil de tolerence):
				if (nbEch/3 < i &&  i < 2 * nbEch/3) {
					somme += value;	// (j + i) correspond a l'indice total.
				}
			}
			*/
			
			if (indice == nbEch) {
				moyenne = somme/nbEchUtils;
				if(moyenne > center) { // Remplacer Amax par Amax - seuilTolerence 
					informationGeneree.add(true);
				}
				else {
					informationGeneree.add(false);
				}
				indice = 0;
				somme = 0;
				moyenne = 0;
				nbEchUtils = 0;
			}
		}
	}
	
	/**
	 * Convertit l'information analogique NRZ recue en booleen. 
	 */
	protected void convertFromNRZ() {
		float center = (Amax+Amin)/2;
		int compteur = 0;
		float somme = 0;
		float moyenne = 0;
		for (float value: informationRecue) {
			compteur++;
			somme += value;
			moyenne = somme/(nbEch);
			if(compteur >= nbEch) {
				if (moyenne >= center) { // Remplacer (Amax) par (Amax - seuilTolerence)  
					informationGeneree.add(true);
					somme = 0;
					moyenne = 0;
					compteur = 0;
				}
				else {
					informationGeneree.add(false); // Le cas ou le symbole est centre en 0. N'arrive jamais sans bruit normalement.
					somme = 0;
					moyenne = 0;
					compteur = 0;
				}
			}
		}
	}	
	
	/**
	 * Convertit l'information analogique NRZT recue en booleen.
	 */
	protected void convertFromNRZT() {
		int compteur = 0;
		float somme = 0.0f;
		float moyenne = 0;
		float center = (Amax+Amin)/2;
		for (float value: informationRecue) {
			if (compteur >= nbEch/3 && compteur < 2 * nbEch/3) {
				somme += value;
			}
			compteur++;
			
			if (compteur == nbEch) {
				compteur = 0;
				moyenne = 3 * somme/nbEch;		// Car on fait somme/(nbEch/3), ce qui est mieux optimise ecrit comme ca.
				if (moyenne > center) { // TODO: A terme, remplacer Amax par Amax - seuilTolerence <-- modifie par center. Si la moyenne est positive par rapport au centre, 1. 0 sinon.
					informationGeneree.add(true);
				}
				else {
					informationGeneree.add(false);
				}
				moyenne = 0.0f;
				somme = 0.0f;
			}
		}
	}
	
	//* a faire pour le calcul du SNR
	public Information<Float> getSignalAnalogiqueSortie() {
		return this.informationRecue;
	}
	//*/
}
