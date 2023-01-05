package transmetteurs;

import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedList;
import java.util.Random;

import destinations.DestinationInterface;
import information.Information;
import information.InformationNonConformeException;

/**
 * Un transmetteur a bruit blanc additif gaussien pour des messages analogiques.
 * Cette classe sert typiquement a lier un emetteur et un recepteur analogiques pour des simulations bruitees.
 * 
 * @author groupeA3
 * @date 2022/09/22
 */
public class TransmetteurAnalogiqueBruite extends Transmetteur<Float,Float> {
	
	/**
	 * Information contenant le bruit seul.
	 */
	protected Information<Float> informationBruit;
	
	/**
	 * Information contenant la somme du signal et du bruit.
	 */
	protected Information<Float> informationSignalBruit;
	
	/**
	 * Rapport signal a bruit en dB.
	 */
    protected float SNR;

    /**
	 * Nombre d'echantillons par symbole.
	 */
    protected int nbEch;

    /**
	 * Precise si oui ou non le bruit utilise une graine de generation.
	 */
    protected boolean utiliseSeed;

    /**
	 * Graine de generation du bruit.
	 */
    protected int seed;

    /**
     * Le constructeur instancie SNR et nbEch aux valeurs passees et met les informations a null pour le moment.
     * 
     * @param SNR - Le rapport signal a bruit par symbole en dB
     * @param nbEch - Le nombre d'echantillons par symbole
     */
    public TransmetteurAnalogiqueBruite(float SNR, int nbEch) {
        super();
        
        this.SNR = SNR;
        this.nbEch = nbEch;
        this.utiliseSeed = false;
        informationBruit = null;
        informationSignalBruit = null;
    }

    /**
     * Le constructeur instancie SNR, nbEch et seed aux valeurs passees et met les informations a null pour le moment.
     * Ce constructeur alternatif est a utiliser pour de la transmission bruitee reproductible (avec graine).
     * 
     * @param SNR - Le rapport signal a bruit par symbole en dB
     * @param nbEch - Le nombre d'echantillons par symbole
     * @param seed - La graine de generation du bruit
     */
    public TransmetteurAnalogiqueBruite(float SNR, int nbEch, int seed) {
        super();
        
        this.SNR = SNR;
        this.nbEch = nbEch;
        this.seed = seed;
        this.utiliseSeed = true;
        informationBruit = null;
        informationSignalBruit = null;
    }

	/**
	 * Methode d'emission de l'information generee vers les transmetteurs connectes.
	 * Elle intervient typiquement apres generation du bruit car elle l'ajoute avant d'emettre.
	 */
	@Override
	public void emettre() throws InformationNonConformeException {
		ajouterBruit();
		informationEmise = informationSignalBruit;
		for (DestinationInterface<Float> destinationConnectee: destinationsConnectees) {
            destinationConnectee.recevoir(informationEmise);
        }
	}
	
	/**
	 * Methode d'emission de l'information generee vers les transmetteurs connectes.
	 * Elle met en parametre optionnel bruite qui ajoute le bruit sur demande.
	 * Cette methode est toujours sense etre utilisee.
	 */
	public void emettre(boolean bruite) throws InformationNonConformeException {
		if(bruite) {
			ajouterBruit();
			informationEmise = informationSignalBruit;
			for (DestinationInterface<Float> destinationConnectee: destinationsConnectees) {
	            destinationConnectee.recevoir(informationEmise);
	        }
		}
		else {
			for (DestinationInterface<Float> destinationConnectee: destinationsConnectees) {
	            destinationConnectee.recevoir(informationRecue);
	        }
		}
	}
	
	/**
	 * Permet de recevoir une information, d'y ajouter du bruit et de la transmettre.
	 * 
	 * @param information - Information a recevoir
	 * @throws InformationNonConformeException
	 */
	@Override
	public void recevoir(Information<Float> information) throws InformationNonConformeException {
		this.informationRecue = information;
		genererBruit(calculSigma());
		emettre();
	}

	/**
	 * Ajoute le bruit au signal recu.
	 */
	protected void ajouterBruit() {
		informationSignalBruit = new Information<Float>();

		for (int i = 0; i < informationRecue.nbElements(); i++) {
			informationSignalBruit.add((informationBruit.iemeElement(i) + informationRecue.iemeElement(i)));
		}
	}

	/**
	 * Genere le bruit a partir d'un ecart-type donne et, si applicable, avec la graine fournie. On suppose le bruit centre.
	 * 
	 * @param sigmaB
	 */
	protected void genererBruit(float sigmaB) {
		informationBruit = new Information<Float>();

		Random a1;
		Random a2;
		if (utiliseSeed) {
			a1 = new Random(this.seed);
			a2 = new Random(this.seed);
		} else {
			a1 = new Random();
			a2 = new Random();
		}

		for (int i = 0; i < informationRecue.nbElements(); i++) {
			float b;
			b = (float)(sigmaB*Math.sqrt(-2*Math.log(1-a1.nextFloat()))*Math.cos(2*Math.PI*a2.nextFloat()));
			informationBruit.add(b); 
		}
	}

	/**
	 * Calcule l'ecart-type a partir du SNR.
	 * 
	 * @return sigma - L'ecart-type du bruit blanc gaussien voulu par l'ecart-type fourni
	 */
	public float calculSigma() {
		float sigma;
		float ps = calculPuissance(informationRecue);
		float logSNR = (float) Math.pow(10, SNR / 10);
		sigma = (float) Math.sqrt((nbEch * ps) / (2 * logSNR));
		return sigma;
	}

	/**
	 * Calcule la puissance moyenne d'une information analogique.
	 * 
	 * @param informationAnalogique - L'informaton a mesurer
	 * @return puissanceMoyenneSignal - La puissance moyenne du signal
	 */
	public float calculPuissance(Information<Float> informationAnalogique) {
		float puissanceMoyenneSignal = 0;
		float sommeSignaux = 0;
		for (float f: informationAnalogique) {
			sommeSignaux += Math.pow(f,2);
		}
		puissanceMoyenneSignal = sommeSignaux / informationAnalogique.nbElements();
		return puissanceMoyenneSignal;
	}

	/**
	 * Permet d'ecrire les donnees du bruit au format CSV.
	 */
	public void bruitCSV() {
		if (informationBruit != null) {
			try {
				int arrondi;
				boolean ajout;
				LinkedList<int[]> list = new LinkedList<>();
				FileWriter fichier = new FileWriter("valeursBruit.csv");
				for (float f:informationBruit) {
					ajout=false;
					arrondi = Math.round(f * 10);
					for (int[] listInt : list) {
						if (listInt[0] == arrondi) {
							listInt[1]++;
							ajout = true;
							break;
						}
					}
					if (ajout == false) {
						list.add(new int[]{arrondi, 1});
					}	
				}
				for (int[] listInt : list) {
                    fichier.write(listInt[0] + ";" + listInt[1] + ";\n");
                }
				fichier.close();

			}
			catch (IOException e){
				e.printStackTrace();
			}
		}
	}
}
