package simulateur;


import java.io.FileWriter;

import destinations.Destination;
import destinations.DestinationFinale;

import information.Information;

import sources.Source;
import sources.SourceFixe;
import sources.SourceAleatoire;

import transmetteurs.Transmetteur;
import transmetteurs.TransmetteurParfait;
import transmetteurs.Codeur;
import transmetteurs.Decodeur;
import transmetteurs.Emetteur;
import transmetteurs.TransmetteurAnalogique;
import transmetteurs.TransmetteurAnalogiqueBruite;
import transmetteurs.TransmetteurAnalogiqueMultiTrajet;
import transmetteurs.Recepteur;

import visualisations.*;

/** La classe Simulateur permet de construire et simuler une chaine de
 * transmission composee d'une Source, d'un nombre variable de
 * Transmetteur(s) et d'une Destination.
 * @author cousin
 * @author prou
 * @author groupeA3
 */
public class Simulateur {
    /** indique si le Simulateur utilise des sondes d'affichage */
    private boolean affichage = false;
    
    /** indique si le Simulateur utilise un message genere de maniere aleatoire (message impose sinon) */
    private boolean messageAleatoire = true;
    
    /** indique si le Simulateur utilise un germe pour initialiser les generateurs aleatoires */
    private boolean aleatoireAvecGerme = false;
    
    /** la valeur de la semence utilisee pour les generateurs aleatoires correspondant a seed dans l'argument -seed s */
    private Integer seed = null; // pas de semence par defaut
    
    /** la longueur du message aleatoire a transmettre si un message n'est pas impose */
    private int nbBitsMess = 100; 
    
    /** la chaine de caracteres correspondant a m dans l'argument -mess m */
    private String messageString = "100";

    /** forme d'onde pour la transmission */
    private String forme = "RZ";

    /** nombre d'echantillons par bit correspondant a nb dans l'argument -nbEch ne */
    private int nbEch = 30;

    /** amplitude maximale correspondant a max dans l'argument -ampl min max */
    private float Amax = 1.0f;

    /** amplitude minimale correspondant a min dans l'argument -ampl min max */
    private float Amin = 0.0f;
    
    /** valeur du rapport signal sur bruit en dB */

    private float SNR = 10;
    
    /** valeurs des facteurs d'amplitude des signaux en multitrajet **/
    private Float[] ar = {};
    
    /** valeurs de retard des signaux en multitrajet **/
    private int[] dt = {};

    /** le composant Source de la chaine de transmission */
    private Source <Boolean> source = null;

    /** le composant Transmetteur logique de la chaine de transmission */
    private Transmetteur<Boolean, Boolean> transmetteurLogique = null;

    /** le composant Transmetteur analogique de la chaine de transmission */
    private Transmetteur<Float, Float> transmetteurAnalogique = null;

    /** le composant Destination de la chaine de transmission */
    private Destination <Boolean> destination = null;

    /** le composant Codeur de la chaine de transmission */
    private Transmetteur<Boolean, Boolean> codeur = null;

    /** le composant Decodeur logique de la chaine de transmission */
    private Transmetteur<Boolean, Boolean> decodeur = null;

    /** indique si la transmission est logique (booleenne) ou analogique */
    private boolean analogique = false;

    /** indique si la transmission est bruite */
    private boolean bruite = false;

    /** indique si la transmission est en multitrajet */
    private boolean multitrajet = false;

    /** indique si on utilise un codage de signal */
    private boolean codage = false;
    
    /** le composant Emetteur de la chaine de transmission */
    private Transmetteur<Boolean,Float> emetteur = null;

    /** le composant Recepteur de la chaine de transmission */
    private Transmetteur<Float,Boolean> recepteur = null;

    /** Le constructeur de Simulateur construit une chaine de
     * transmission composee d'une Source <Boolean>, d'une Destination
     * <Boolean> et de Transmetteur(s) [voir la methode
     * analyseArguments]...  <br> Les differents composants de la
     * chaine de transmission (Source, Transmetteur(s), Destination,
     * Sonde(s) de visualisation) sont crees et connectes.
     * @param args le tableau des differents arguments.
     *
     * @throws ArgumentsException si un des arguments est incorrect
     * @throws InformationNonConformeException
     */   
    public Simulateur(String [] args) throws ArgumentsException {
    	// analyser et recuperer les arguments   	
    	analyseArguments(args);

    	if (messageAleatoire) {
    		if (aleatoireAvecGerme) {
    			source = new SourceAleatoire(nbBitsMess, seed);
    		} else {
    			source = new SourceAleatoire(nbBitsMess);
    		}
    	} else {
    		source = new SourceFixe(messageString);
    	}

    	codeur = new Codeur();
		emetteur = new Emetteur(forme, nbEch, Amin, Amax);
    	transmetteurLogique = new TransmetteurParfait();
		if (multitrajet) {
			if (aleatoireAvecGerme)
				transmetteurAnalogique = new TransmetteurAnalogiqueMultiTrajet(SNR, nbEch, seed, ar, dt, bruite);
			else
				transmetteurAnalogique = new TransmetteurAnalogiqueMultiTrajet(SNR, nbEch, ar, dt, bruite);
			
		}
		else if (bruite) {
			if (aleatoireAvecGerme)
				transmetteurAnalogique = new TransmetteurAnalogiqueBruite(SNR, nbEch, seed);
			else
				transmetteurAnalogique = new TransmetteurAnalogiqueBruite(SNR, nbEch);
		}
		else
			transmetteurAnalogique = new TransmetteurAnalogique(); // transmetteur parfait/ideal
		recepteur = new Recepteur(forme, nbEch, Amin, Amax);
		decodeur = new Decodeur();
    	destination = new DestinationFinale();
    }   

    /** La methode analyseArguments extrait d'un tableau de chaines de
     * caracteres les differentes options de la simulation.
     * <br>Elle met a jour les attributs correspondants du Simulateur.
     *
     * @param args le tableau des differents arguments.
     * <br>
     * <br>Les arguments autorises sont : 
     * <br> 
     * <dl>
     * <dt> -mess m  </dt><dd> m (String) constitue de 7 ou plus digits a 0 | 1, le message a transmettre</dd>
     * <dt> -mess m  </dt><dd> m (int) constitue de 1 a 6 digits, le nombre de bits du message "aleatoire" a transmettre</dd>
     * <dt> -s </dt><dd> pour demander l'utilisation des sondes d'affichage</dd>
     * <dt> -seed v </dt><dd> v (int) d'initialisation pour les generateurs aleatoires</dd>
     * <dt> -form f </dt><dd> f (String) de valeur "RZ", "NRZ" ou "NRZT", la forme d'onde dans le cas analogique (rend le message analogique si spécifié)</dd>
     * <dt> -nbEch ne</dt><dd> ne (int) nombre entier positif, le nombre d'echantillons par bit (rend le message analogique si spécifié)</dd>
     * <dt> -ampl min max</dt><dd> min (float) et max (float) flottants tels que min est strictement inférieur a max, les amplitudes min et max (rend le message analogique si spécifié)</dd>
     *
     * @throws ArgumentsException si un des arguments est incorrect.
     */   
    public void analyseArguments(String[] args) throws ArgumentsException {

    	for (int i = 0; i < args.length; i++) { // traiter les arguments 1 par 1

    		if (args[i].matches("-s")) {
    			affichage = true;
    		}

    		else if (args[i].matches("-seed")) {
    			aleatoireAvecGerme = true;
    			i++; 
    			// traiter la valeur associee
    			try { 
    				seed = Integer.valueOf(args[i]);
    			}
    			catch (Exception e) {
    				throw new ArgumentsException("Valeur du parametre -seed  invalide : " + args[i]);
    			}           		
    		}
    		else if (args[i].matches("-mess")) {
    			i++; 
    			// traiter la valeur associee
    			messageString = args[i];
    			if (args[i].matches("[0,1]{7,}")) { // au moins 7 digits
    				messageAleatoire = false;
    				nbBitsMess = args[i].length();
    			} 
    			else if (args[i].matches("[0-9]{1,6}")) { // de 1 a 6 chiffres
    				messageAleatoire = true;
    				nbBitsMess = Integer.valueOf(args[i]);
    				if (nbBitsMess < 1) 
    					throw new ArgumentsException("Valeur du parametre -mess invalide : " + nbBitsMess);
    			}
    			else 
    				throw new ArgumentsException("Valeur du parametre -mess invalide : " + args[i]);
    		}

    		else if (args[i].matches("-form")) {
    			i++;
    			// traiter la valeur associee
    			if (args[i].matches("(RZ|NRZ|NRZT)")) { // exactement "RZ", "NRZ" ou "NRZT"
        			forme = args[i];
        			analogique = true;
    			}
    			else
    				throw new ArgumentsException("Valeur du parametre -form invalide : " + args[i]);
    		}

    		else if (args[i].matches("-nbEch")) {
    			i++;
    			// traiter la valeur associee
    			if (args[i].matches("[0-9]+")) { // entier
    				nbEch = Integer.valueOf(args[i]);
        			analogique = true;
    				if (nbEch < 3) // prise de libertés pour éviter les résultats aberrants
    					throw new ArgumentsException("Valeur du parametre -nbEch trop faible (minimum 3) : " + nbEch);
    			}
    			else
    				throw new ArgumentsException("Valeur du parametre -nbEch invalide : " + args[i]);
    		}

    		else if (args[i].matches("-ampl")) {
    			i += 2;
    			// traiter la premiere valeur associee
    			if (args[i].matches("^[+-]?([0-9]+\\.?[0-9]*|\\.[0-9]+)$")) { // decimal
    				Amax = Float.parseFloat(args[i]);
    			}
    			else
    				throw new ArgumentsException("Valeur d'un parametre -ampl invalide : " + args[i]);

    			if (args[i-1].matches("^[+-]?([0-9]+\\.?[0-9]*|\\.[0-9]+)$")) { // decimal
    				Amin = Float.parseFloat(args[i-1]);
    			}
    			else
    				throw new ArgumentsException("Valeur d'un parametre -ampl invalide : " + args[i-1]);

    			if (!(Amin < Amax))
    				throw new ArgumentsException("Valeur des parametres -ampl invalide : " + args[i] + " (max) est inferieur ou egal a " + args[i-1] + " (min)");
    			analogique = true;
    		}

    		else if (args[i].matches("-snrpb")) {
    			i += 1;
    			// traiter la premiere valeur associee
    			if (args[i].matches("^[+-]?([0-9]+\\.?[0-9]*|\\.[0-9]+)$")) { // decimal
    				SNR = Float.parseFloat(args[i]);
    				analogique = true;
    				bruite = true;
    			}
    			else {
    				throw new ArgumentsException("Valeur d'un parametre -ampl invalide : " + args[i]);
    			}
    		}

    		else if (args[i].matches("-ti")) {
    			// traiter le premier couple associe
    			while (args.length - i > 2 && args[i+1].matches("[0-9]+") && args[i+2].matches("^[+]?([0-9]+\\.?[0-9]*|\\.[0-9]+)$")) {
    				// permet d'ajouter la nouvelle valeur dans les deux listes ar et dt
    				Float[] new_ar_tab = new Float[ar.length+1];
    				int[] new_dt_tab = new int[dt.length+1];
    				for (int j = 0; j < dt.length; j++) {
    					new_ar_tab[j] = ar[j];
    					new_dt_tab[j] = dt[j];
    				}
    				Float new_ar = Float.parseFloat(args[i+2]);
    				int new_dt = Integer.valueOf(args[i+1]);
    				if (new_ar < 0 || new_ar > 1)
    					throw new ArgumentsException("La valeur de ar " + new_ar + " n'est pas comprise entre 0 et 1");
    				if (new_dt < 0)
    					throw new ArgumentsException("La valeur de dt " + new_dt + " est négative");
    				new_ar_tab[new_ar_tab.length-1] = new_ar;
    				new_dt_tab[new_dt_tab.length-1] = new_dt;
    				ar = new_ar_tab;
    				dt = new_dt_tab;
    				analogique = true;
    				multitrajet = true;
        			i += 2;
    			}
    			// chercher a comprendre pourquoi on est sorti de la boucle
    			if (args.length - i <= 1)
    				break;
    			if ((args.length - i == 2 && !args[i+1].startsWith("-")) || (args[i+1].matches("[0-9]+") && !args[i+2].matches("^[+]?([0-9]+\\.?[0-9]*|\\.[0-9]+)$") && args[i+2].startsWith("-"))) {
    				// s'il y a un argument seul dans son couple
    				throw new ArgumentsException("Les parametres de -ti viennent par couples, or " + args[i+1] + " est orphelin");
    			} else if (args.length - i == 2) {
    				break;
    			} else if (args[i+1].startsWith("-")) {
    				continue;
    			} else if (!args[i+1].matches("[0-9]+")) {
    				// si ce n'est pas un autre argument mais que c'est invalide (ca ne peut pas etre un negatif car on serait deja sorti)
    				throw new ArgumentsException("Parametre invalide pour -ti : " + args[i+1]);
    			} else if (!args[i+2].matches("^[+]?([0-9]+\\.?[0-9]*|\\.[0-9]+)$")) {
    				// idem pour le deuxieme
    				throw new ArgumentsException("Parametre invalide pour -ti : " + args[i+2]);
    			}
				if (ar.length > 5) {
					throw new ArgumentsException("Le nombre de multitrajet est trop élevé");
				}
    		}

    		else if (args[i].matches("-codeur")) {
    			codage = true;
    		}

    		else throw new ArgumentsException("Option invalide : " + args[i]);
    	}

    	if (ar.length > 5 || dt.length > 5) {
			throw new ArgumentsException("Le nombre de couples multitrajet est trop eleve");
		}
    	for (int valeurDt: dt) {
    		if (valeurDt >= nbBitsMess * nbEch)
    			throw new ArgumentsException("Le decalage " + valeurDt + " depasse la taille du message");
    	}
    }

    /** La methode execute effectue un envoi de message par la source
     * de la chaine de transmission du Simulateur.
     *
     * @throws Exception si un probleme survient lors de l'execution
     *
     */ 
    public void execute() throws Exception {

    	if (analogique) {
    		if (!codage) {
    			source.connecter(emetteur);
    			recepteur.connecter(destination);
    		} else {
    			// source connectee au codeur juste apres
    			codeur.connecter(emetteur);
    			recepteur.connecter(decodeur);
    			// decodeur connecte a la destination juste apres
    		}
			emetteur.connecter(transmetteurAnalogique);
			transmetteurAnalogique.connecter(recepteur);
			// connexion des sondes si l'affichage est active
			if (affichage) {
				source.connecter(new SondeLogique("Source", 200));
				emetteur.connecter(new SondeAnalogique("Emetteur"));
				transmetteurAnalogique.connecter(new SondeAnalogique("Transmetteur"));
				recepteur.connecter(new SondeLogique("Recepteur", 200));
			}
		} else {
			if (!codage) {
				source.connecter(transmetteurLogique);
				transmetteurLogique.connecter(destination);
			} else {
    			// source connectee au codeur juste apres
				codeur.connecter(transmetteurLogique);
				transmetteurLogique.connecter(decodeur);
    			// decodeur connecte a la destination juste apres
			}
			// connexion des sondes si l'affichage est active
			if (affichage) {
				source.connecter(new SondeLogique("Source", 200));
				transmetteurLogique.connecter(new SondeLogique("Transmetteur", 200));
			}
		}
		if (codage) {
    		source.connecter(codeur);
    		decodeur.connecter(destination);
			if (affichage) {
				codeur.connecter(new SondeLogique("Codeur", 200));
				decodeur.connecter(new SondeLogique("Decodeur", 200));
			}
		}

      	source.emettre();
    }

    /**
     * La methode qui calcule le taux d'erreur binaire (TEB) en
     * comparant les bits du message emis avec ceux du message recu.
     * 
     * En cas de difference de longueur, chaque bit en trop ou en moins compte faux.
     * Il faut donc faire attention si le TEB est proche de 1 : c'est peut-etre une erreur de longueur.
     * 
     * @return - La valeur du TEB, comprise entre 0.0 et 1.0.
     */   	   
    public float calculTauxErreurBinaire() {

    	Information<Boolean> messageEmis = this.source.getInformationEmise();
    	Information<Boolean> messageRecu = this.destination.getInformationRecue();

    	int longueurEmise = messageEmis.nbElements();
    	int longueurRecue = messageRecu.nbElements();
    	int longueurMinimale, longueurMaximale;
    	if (longueurEmise <= longueurRecue) {
    		longueurMinimale = longueurEmise;
    		longueurMaximale = longueurRecue;
    	} else {
    		longueurMinimale = longueurRecue;
    		longueurMaximale = longueurEmise;
    	}

    	int nbErreurs = 0;
    	for (int i = 0; i < longueurMinimale; i++) {
    		if (messageEmis.iemeElement(i) != messageRecu.iemeElement(i)) {
    			nbErreurs++;
    		}
    	}

    	return (float) (nbErreurs + (longueurMaximale - longueurMinimale)) / longueurMaximale;
    }

	/** La fonction main instancie un Simulateur a l'aide des
     *  arguments parametres et affiche le resultat de l'execution
     *  d'une transmission.
     *  @param args les differents arguments qui serviront a l'instanciation du Simulateur.
     */
    public static void main(String [] args) { 

    	Simulateur simulateur = null;

    	try {
    		simulateur = new Simulateur(args);
    	}
    	catch (Exception e) {
    		System.out.println(e); 
    		System.exit(-1);
    	} 

    	try {
    		simulateur.execute();
    		String s = "java  Simulateur  ";
    		for (int i = 0; i < args.length; i++) { // copier tous les parametres de simulation
    			s += args[i] + "  ";
    		}
    		System.out.println(s + "  =>   TEB : " + simulateur.calculTauxErreurBinaire());
    		
    	}
    	catch (Exception e) {
    		System.out.println(e);
    		e.printStackTrace();
    		System.exit(-2);

    	}
    }
}
