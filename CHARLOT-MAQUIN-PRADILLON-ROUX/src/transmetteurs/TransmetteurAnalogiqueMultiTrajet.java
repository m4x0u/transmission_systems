package transmetteurs;

import java.util.LinkedList;

import information.Information;
import information.InformationNonConformeException;

/**
 * Un transmetteur multitrajet pour des messages analogiques.
 * Cette classe sert typiquement a lier un emetteur et un recepteur analogiques pour des simulations bruitees multitrajet.
 * On pourra optionnellement ajouter un bruit blanc gaussien.
 * 
 * @author groupeA3
 * @date 2022/09/29
 */
public class TransmetteurAnalogiqueMultiTrajet extends TransmetteurAnalogiqueBruite {
	
	/**
	 * Information contenant la somme des signaux avec multitrajet et du bruit.
	 */
	protected Information<Float> informationSignalMultitrajet;

    /**
	 * Liste des rapport d'amplitude (en volts) des multitrajets.
	 */
    protected Float[] ar;

    /**
	 * Liste des decalages des multitrajets (en echantillons).
	 */
    protected int[] dt;
    
    /**
     * Indique si on ajoute en plus un bruit blanc gaussien.
     */
    protected boolean bruite;

    /**
     * Le constructeur instancie SNR, nbEch, ar et dt aux valeurs passees et met les informations a null pour le moment.
     * 
     * @param nbEch
     * @param SNR
	 * @param ar
     * @param dt
     */
    public TransmetteurAnalogiqueMultiTrajet(float SNR, int nbEch, Float[] ar, int[] dt, boolean bruite) {
        super(SNR, nbEch);
        this.bruite = bruite;
        this.ar = ar;
        this.dt = dt;
        informationSignalMultitrajet = null;
    }

    /**
     * Le constructeur instancie SNR, nbEch, seed, ar et dt aux valeurs passees et met les informations a null pour le moment.
     * Ce constructeur alternatif est a utiliser pour de la transmission bruitee multitrajet reproductible (avec graine).
     * 
     * @param nbEch
     * @param SNR
     * @param seed
	 * @param ar
     * @param dt
     */
    public TransmetteurAnalogiqueMultiTrajet(float SNR, int nbEch, int seed, Float[] ar, int[] dt, boolean bruite) {
        super(SNR, nbEch, seed);
        this.bruite = bruite;
        this.ar = ar;
        this.dt = dt;
        informationSignalMultitrajet = null;
    }

	/**
	 * Permet de recevoir une information, d'y ajouter les trajets alternatifs et du bruit et de la transmettre.
	 * 
	 * @param information - Information a recevoir
	 * @throws InformationNonConformeException
	 */
	@Override
	public void recevoir(Information<Float> information) throws InformationNonConformeException {
		this.informationRecue = information;
		genererMultitrajet();
		if (bruite) {
			genererBruit(calculSigma());
		}
		bruitCSV();
		emettre(bruite);
	}
	
	/**
	 * Ajoute le bruit au signal recu.
	 */
	@Override
	protected void ajouterBruit() {
		informationSignalBruit = new Information<Float>();

		for (int i = 0; i < informationRecue.nbElements(); i++) {
			informationSignalBruit.add((informationBruit.iemeElement(i) + informationSignalMultitrajet.iemeElement(i)));
		}
	}

	/**
	 * Fonction creant le signal multitrajet.
	 */
	protected void genererMultitrajet() {
		LinkedList<Integer> blacklist = new LinkedList<Integer>();

		for (int i = 0; i < dt.length; i++) {
			if (dt[i] >= informationRecue.nbElements()) {
				blacklist.add(i);
			}
		}
		int new_size = dt.length - blacklist.size();
		int[] new_dt = new int[new_size];
		Float[] new_ar = new Float[new_size];
		
		for (int i = 0; i < dt.length; i++) {
			if (!blacklist.contains(dt[i])) {
				new_dt[i] = dt[i];
				new_ar[i] = ar[i];
			}
		}
		ar = new_ar;
		dt = new_dt;

		/*
		// FAMT pour Facteur d'Attenuation par Multitrajet. Cela permet de repartir la puissance entre les signaux :
		float FAMT = 0.0f;
		for (int i=0; i<ar.length; i++) {
			FAMT += ar[i];
		}
		// FAMT += 1; Car dans le cahier des charges, on somme s(t) avec les retards, et non pas la moyenne avec s(t)
		FAMT = 1/FAMT;
		*/

		informationSignalMultitrajet = new Information<Float>();
		// On parle de p_dt pour position dans dt
		for (int i = 0; i < informationRecue.nbElements(); i++) {
			float nouvelIemeElement = 0.0f;
			for (int p_dt = 0; p_dt < dt.length; p_dt++) {
				if (dt[p_dt] <= i) {
					nouvelIemeElement += ar[p_dt] * informationRecue.iemeElement(i - dt[p_dt]);
				}
			}
			informationSignalMultitrajet.add(informationRecue.iemeElement(i) + nouvelIemeElement);
		}
		if (!bruite) {
			informationRecue = informationSignalMultitrajet;
		}
	}
}
