package serveur.interaction;

import java.awt.Point;
import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.logging.Level;

import serveur.Arene;
import serveur.element.Caracteristique;
import serveur.element.Personnage;
import serveur.vuelement.VuePersonnage;
import utilitaires.Calculs;
import utilitaires.Constantes;

public class Clairvoyance extends Interaction<VuePersonnage> {

	public Clairvoyance(Arene arene, VuePersonnage attaquant, VuePersonnage defenseur) {
		super(arene, attaquant, defenseur);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void interagit() {
		// TODO Auto-generated method stub
		try {
			Personnage pDefenseur = defenseur.getElement();
			
			logs(Level.INFO, Constantes.nomRaccourciClient(defenseur) + 
					" =>  Vie : "+ pDefenseur.getCaract(Caracteristique.VIE)
					+ " Force : " +  pDefenseur.getCaract(Caracteristique.FORCE)
					+ "Initiative : " +  pDefenseur.getCaract(Caracteristique.INITIATIVE));
		} // try
		catch (Exception e) {
			logs(Level.INFO, "\nErreur lors d'une clairvoyance : " + e.toString());
		} // catch 
	}

}
