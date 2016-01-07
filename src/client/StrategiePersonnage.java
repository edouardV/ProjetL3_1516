package client;


import java.awt.Point;
import java.rmi.RemoteException;
import java.util.HashMap;

import client.controle.Console;
import logger.LoggerProjet;
import serveur.IArene;
import serveur.element.Caracteristique;
import serveur.element.Element;
import serveur.element.Personnage;
import serveur.element.Potion;
import utilitaires.Calculs;
import utilitaires.Constantes;

/**
 * Strategie d'un personnage. 
 */
public class StrategiePersonnage {
	
	/**
	 * Console permettant d'ajouter une phrase et de recuperer le serveur 
	 * (l'arene).
	 */
	protected Console console;

	/**
	 * Cree un personnage, la console associe et sa strategie.
	 * @param ipArene ip de communication avec l'arene
	 * @param port port de communication avec l'arene
	 * @param ipConsole ip de la console du personnage
	 * @param nom nom du personnage
	 * @param groupe groupe d'etudiants du personnage
	 * @param nbTours nombre de tours pour ce personnage (si negatif, illimite)
	 * @param position position initiale du personnage dans l'arene
	 * @param logger gestionnaire de log
	 */
	public StrategiePersonnage(String ipArene, int port, String ipConsole, 
			String nom, String groupe, HashMap<Caracteristique, Integer> caracts,
			int nbTours, Point position, LoggerProjet logger) {
		
		logger.info("Lanceur", "Creation de la console...");
		try {
			console = new Console(ipArene, port, ipConsole, this, 
					new Personnage(nom, groupe, caracts), 
					nbTours, position, logger);
			logger.info("Lanceur", "Creation de la console reussie");
			
		} // try 
		catch (Exception e) {
			logger.info("Personnage", "Erreur lors de la creation de la console : \n" + e.toString());
			e.printStackTrace();
		} // catch 
	} //StrategiePersonnage

	// TODO etablir une strategie afin d'evoluer dans l'arene de combat
	// une proposition de strategie (simple) est donnee ci-dessous
	/** 
	 * Decrit la strategie.
	 * Les methodes pour evoluer dans le jeu doivent etre les methodes RMI
	 * de Arene et de ConsolePersonnage. 
	 * @param voisins element voisins de cet element (elements qu'il voit)
	 * @throws RemoteException
	 */
	public void executeStrategie(HashMap<Integer, Point> voisins) throws RemoteException {
		// arene
		IArene arene = console.getArene();
		
		// reference RMI de l'element courant
		int refRMI = 0;
		
		// position de l'element courant
		Point position = null;
		
		try {
			refRMI = console.getRefRMI();
			position = arene.getPosition(refRMI);
		} // try
		catch (RemoteException e) {
			e.printStackTrace();
		} // catch
		
		/* recuperer les caracteristiques du personnage */
		HashMap<Caracteristique, Integer> caracts = console.getPersonnage().getCaracts();
		
		
		/* selon le type de personnage, executer la strategie correspondante */
		/* type 0 : Personnage basique */
		if (caracts.get(Caracteristique.TYPE_PERSO) == 0)
		{
			
			if (voisins.isEmpty()) { // si je n'ai pas de voisin, j'erre
				console.setPhrase("J'erre...");
				arene.deplace(refRMI, 0); 	
			} // if 
			
			else {
				int refCible = Calculs.chercheElementProche(position, voisins);
				int distPlusProche = Calculs.distanceChebyshev(position, arene.getPosition(refCible));

				Element elemPlusProche = arene.elementFromRef(refCible);

				if(distPlusProche <= Constantes.DISTANCE_MIN_INTERACTION) { // si l'element voisin le plus proche est a portee
					// j'interagis directement
					if(elemPlusProche instanceof Potion) { // si c'est une potion
						// je la ramassage
						console.setPhrase("Je ramasse une potion");
						arene.ramassePotion(refRMI, refCible);
					} // if  
					else { // si c'est un personnage
						// je lance le duel
						console.setPhrase("Je fais un duel avec " + elemPlusProche.getNom());
						arene.lanceAttaque(refRMI, refCible);
					} // else 
				} // if 
				else { // si le voisin le plus proche n'est pas a portee
					// je vais vers lui
					console.setPhrase("Je vais vers mon voisin " + elemPlusProche.getNom());
					arene.deplace(refRMI, refCible);
				} // else 
			} // else 
			
		} // if
		
		
		/* type 1 : Peureux (evite les potions) */
		if (caracts.get(Caracteristique.TYPE_PERSO) == 1)
		{
			
			if (voisins.isEmpty()) { // si je n'ai pas de voisin, j'erre
				console.setPhrase("J'erre...");
				arene.deplace(refRMI, 0); 
			} // if 
			
			else {
				int refCible = Calculs.chercheElementProche(position, voisins);
				int distPlusProche = Calculs.distanceChebyshev(position, arene.getPosition(refCible));

				Element elemPlusProche = arene.elementFromRef(refCible);

				if(distPlusProche <= Constantes.DISTANCE_MIN_INTERACTION) { // si l'element voisin le plus proche est a portee
					// j'interagis directement
					if(elemPlusProche instanceof Potion) { // si c'est une potion
						// ne PAS ramasser (errer) 
						console.setPhrase("Une potion ! Je suis sur que c'est un piege...");
						arene.deplace(refRMI, 0);
					} // if  
					else { // si c'est un personnage
						// je l'attaque
						console.setPhrase("Je fais un duel avec " + elemPlusProche.getNom());
						arene.lanceAttaque(refRMI, refCible);
					} // else 
				} // if 
				else { // si le voisin le plus proche n'est pas a portee
					if(elemPlusProche instanceof Potion) { // si c'est une potion
						// l'ignorer (errer)
						console.setPhrase("Une potion...Continuons a errer...");
						arene.deplace(refRMI, 0);
					} // if  
					else { // si c'est un personnage
					// je vais vers lui
					console.setPhrase("Je vais vers le personnage " + elemPlusProche.getNom());
					arene.deplace(refRMI, refCible);
					} // else
				} // else 
			} // else 
			
		} // if
		
		
		/* type 2 : AOE */
		if (caracts.get(Caracteristique.TYPE_PERSO) == 2)
		{
			
			if (voisins.isEmpty()) { // je n'ai pas de voisin, j'erre
				console.setPhrase("J'erre...");
				arene.deplace(refRMI, 0); 
			} // if 
			
			else {
				HashMap<Integer, Point> refCibleMap = Calculs.chercheToutElementProche(position, voisins);
				HashMap<Integer, Point> refPersoAdv = new HashMap<Integer, Point>();	//contient les perso adversaires a portee
				
				for (int refAdv : refCibleMap.keySet() ) {
					Element e = arene.elementFromRef(refAdv);
					int d = Calculs.distanceChebyshev(position, arene.getPosition(refAdv));
					if (e instanceof Personnage && d <= (Constantes.DISTANCE_MAX_INTERACTION))	//les personnages dans la zone de l'aoe (20 de rayon) prendront aussi des degats
						refPersoAdv.put(refAdv, refCibleMap.get(refAdv));
				} // for
				
				int refCible = Calculs.chercheElementProche(position, voisins);
				int distPlusProche = Calculs.distanceChebyshev(position, arene.getPosition(refCible));

				Element elemPlusProche = arene.elementFromRef(refCible);

				if(distPlusProche <= Constantes.DISTANCE_MIN_INTERACTION) {
					// si suffisamment proche
					// j'interagis directement
					if(elemPlusProche instanceof Potion) { // potion
						// ramasser 
						console.setPhrase("Une potion ! Je la bois ");
						arene.ramassePotion(refRMI,refCible);
					} // if  
					else { // personnage
						// attaque AOE
						console.setPhrase("Je lance un AOE sur " + elemPlusProche.getNom());
						arene.AeraOfEffect(refRMI, refPersoAdv);
					} // else 
				} // if 
				else { // si voisin plus proche pas a portee
					// je vais vers lui
					console.setPhrase("Je vais vers mon voisin " + elemPlusProche.getNom());
					arene.deplace(refRMI, refCible);
				} // else 
			} // else 
			
		} // if
		
		
		/* type 3 : soigneur */
		if (caracts.get(Caracteristique.TYPE_PERSO) == 3) 
		{
			
			if (voisins.isEmpty()) { /* pas de voisin */
				/* si j'ai moins de 50 points de vie, je me soigne */
				if (caracts.get(Caracteristique.VIE) <= 50) {
					console.setPhrase("je me soigne");
					arene.Soigner(refRMI, refRMI);
				} // if
				/* sinon j'erre */
				else {
					console.setPhrase("J'erre...");
					arene.deplace(refRMI, 0);
				} // else
			} // if 
			
			else {	/* il y a des voisins */
				int refCible = Calculs.chercheElementProche(position, voisins);
				int distPlusProche = Calculs.distanceChebyshev(position, arene.getPosition(refCible));

				Element elemPlusProche = arene.elementFromRef(refCible);

				if(distPlusProche <= Constantes.DISTANCE_MIN_INTERACTION) { // si suffisamment proche
					// j'interagis directement
					if(elemPlusProche instanceof Potion) { // potion
						// je la bois
						console.setPhrase("Une potion ! Je la bois ");
						arene.ramassePotion(refRMI, refCible);
					} // if  
					else { // personnage
						/* si j'ai moins de 50 points de vie, je me soigne */
						if (caracts.get(Caracteristique.VIE) <= 50) {
							console.setPhrase("je me soigne");
							arene.Soigner(refRMI, refRMI);
						} // if
						/* sinon j'attaque */
						else {
							console.setPhrase("Je fais un duel avec " + elemPlusProche.getNom());
							arene.lanceAttaque(refRMI, refCible);
						} // else 
					} // else 
				} // if 
				else { // si voisin pas a portee
					/* si j'ai moins de 50 points de vie, je me soigne */
					if (caracts.get(Caracteristique.VIE) <= 50) {
						console.setPhrase("je me soigne");
						arene.Soigner(refRMI, refRMI);
					} // if
					/* sinon je vais vers mon voisin */
					else {
						console.setPhrase("Je vais vers mon voisin " + elemPlusProche.getNom());
						arene.deplace(refRMI, refCible);
					} // else
				} // else 
			} // else 
			
		} // if
		
		
		/* type 4 : Assassin */
		if (caracts.get(Caracteristique.TYPE_PERSO) == 4)
		{
			
			if (voisins.isEmpty()) { // je n'ai pas de voisin, j'erre
				console.setPhrase("J'erre...");
				arene.deplace(refRMI, 0); 
			} // if 
			
			else {
				int refCible = Calculs.chercheElementProche(position, voisins);
				int distPlusProche = Calculs.distanceChebyshev(position, arene.getPosition(refCible));

				Element elemPlusProche = arene.elementFromRef(refCible);

				if(distPlusProche <= Constantes.DISTANCE_MIN_INTERACTION) { // si suffisamment proche
					// j'interagis directement
					if(elemPlusProche instanceof Potion) { // potion
						// ramassage
						console.setPhrase("Je ramasse une potion");
						arene.ramassePotion(refRMI, refCible);

					} // if  
					else { // personnage
						// je l'attaque avec une chance de coup critique
						console.setPhrase("Je tente un coup critique sur " + elemPlusProche.getNom());
						arene.lanceCritique(refRMI, refCible);
					} // else 
				} // if 
				else { // si le voisin le plus proche n'est pas a portee
					// je vais vers lui
					console.setPhrase("Je vais vers mon voisin " + elemPlusProche.getNom());
					arene.deplace(refRMI, refCible);
				} // else 
			} // else 
			
		} // if
		
		
		/* type 5 : archer */
		if (caracts.get(Caracteristique.TYPE_PERSO) == 5)
		{
			
			if (voisins.isEmpty()) { // je n'ai pas de voisin, j'erre
				console.setPhrase("J'erre...");
				arene.deplace(refRMI, 0); 
			} // if 
			
			else {
				int refCible = Calculs.chercheElementProche(position, voisins);
				int distPlusProche = Calculs.distanceChebyshev(position, arene.getPosition(refCible));

				Element elemPlusProche = arene.elementFromRef(refCible);

				if(distPlusProche <= Constantes.DISTANCE_MAX_INTERACTION) { // si le voisin le plus proche est a portee de tir (20 cases max)
					// je verifie son type
					if(elemPlusProche instanceof Potion) { // potion
						if(distPlusProche <= Constantes.DISTANCE_MIN_INTERACTION){ //si elle est a portee
							// ramassage
							console.setPhrase("Je ramasse une potion");
							arene.ramassePotion(refRMI, refCible);
						}
						else{	//si elle n'est pas a portee
							// je vais vers elle
							console.setPhrase("Je vais vers la potion " + elemPlusProche.getNom());
							arene.deplace(refRMI, refCible);
						}
					} // if  
					else { // personnage
						// je tire sur lui une fleche
						console.setPhrase("Je tire une fleche sur " + elemPlusProche.getNom());
						arene.TireFleche(refRMI, refCible);
					} // else 
				} // if 
				else { // si le voisin le plus proche n'est pas encore a portee de tir
					// je vais vers lui
					console.setPhrase("Je vais vers mon voisin " + elemPlusProche.getNom());
					arene.deplace(refRMI, refCible);
				} // else 
			} // else 
			
		} // if
		
		
		/* type 6 : mage de feu  */
		if (caracts.get(Caracteristique.TYPE_PERSO) == 6)
		{
			
			if (voisins.isEmpty()) { // je n'ai pas de voisin, j'erre
				console.setPhrase("J'erre...");
				arene.deplace(refRMI, 0); 
			} // if 
			
			else {
				int refCible = Calculs.chercheElementProche(position, voisins);
				int distPlusProche = Calculs.distanceChebyshev(position, arene.getPosition(refCible));

				Element elemPlusProche = arene.elementFromRef(refCible);

				if(distPlusProche <= Constantes.DISTANCE_MIN_INTERACTION) { // si suffisamment proche
					// j'interagis directement
					if(elemPlusProche instanceof Potion) { // potion
						// ramassage
						console.setPhrase("Je ramasse une potion");
						arene.ramassePotion(refRMI, refCible);
					} // if  
					else { // personnage
						// je le brule
						console.setPhrase("Je lance brulure sur " + elemPlusProche.getNom());
						arene.LanceBrulure(refRMI, refCible);
					} // else 
				} // if 
				else { // si voisin le plus proche pas a portee
					// je vais vers lui
					console.setPhrase("Je vais vers mon voisin " + elemPlusProche.getNom());
					arene.deplace(refRMI, refCible);
				} // else 
			} // else 
			
		} // if
		
		
	} // method executeStrategie 	
	
} // class 
