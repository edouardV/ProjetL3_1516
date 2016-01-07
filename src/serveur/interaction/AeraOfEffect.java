package serveur.interaction;

import java.awt.Point;
import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.logging.Level;

import serveur.Arene;
import serveur.element.Caracteristique;
import serveur.element.Personnage;
import serveur.vuelement.VuePersonnage;
import utilitaires.Constantes;


/**
 * Represente une attaque qui inflige des degats a tous les ennemis dans un rayon de 20 cases.
 */
public class AeraOfEffect  extends Interaction<VuePersonnage> {

	public AeraOfEffect(Arene arene, VuePersonnage attaquant, HashMap<Integer, Point> defenseur) {
		super(arene, attaquant, defenseur);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void interagit() {
		try {
			Personnage pAttaquant = attaquant.getElement();
			int forceAttaquant = pAttaquant.getCaract(Caracteristique.FORCE);
			int perteVie = forceAttaquant;
			
			// on parcours tous les personnages sur lesquels on doit infliger les degats de l'AOE
			for (int refAdv : defenseur2.keySet() ) {
				VuePersonnage PersoEnCours = (VuePersonnage) arene.vueFromRef(refAdv);
				Personnage p = PersoEnCours.getElement();
				
				if (perteVie > 0) {
					arene.incrementeCaractElement(PersoEnCours, Caracteristique.VIE, -perteVie);
					System.out.println("trace : " + pAttaquant.getNom() + " aoe sur " + p.getNom());
					logs(Level.INFO, Constantes.nomRaccourciClient(attaquant) + " AOE de "
							+ perteVie + " points de degats) a " + Constantes.nomRaccourciClient(PersoEnCours));
					
					// initiative
					incrementeInitiative(PersoEnCours);
					decrementeInitiative(attaquant);
				}
			}// for 
			
		} catch (RemoteException e) {
			logs(Level.INFO, "\nErreur lors d'une attaque : " + e.toString());
		}
	}

	/**
	 * Incremente l'initiative du defenseur en cas de succes de l'attaque. 
	 * @param defenseur defenseur
	 * @throws RemoteException
	 */
	private void incrementeInitiative(VuePersonnage defenseur) throws RemoteException {
		arene.incrementeCaractElement(defenseur, Caracteristique.INITIATIVE, 
				Constantes.INCR_DECR_INITIATIVE_DUEL);
	}
	
	/**
	 * Decremente l'initiative de l'attaquant en cas de succes de l'attaque. 
	 * @param attaquant attaquant
	 * @throws RemoteException
	 */
	private void decrementeInitiative(VuePersonnage attaquant) throws RemoteException {
		arene.incrementeCaractElement(attaquant, Caracteristique.INITIATIVE, 
				-Constantes.INCR_DECR_INITIATIVE_DUEL);
	}

}

