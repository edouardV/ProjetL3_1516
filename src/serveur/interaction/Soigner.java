package serveur.interaction;

import java.rmi.RemoteException;
import java.util.logging.Level;

import serveur.Arene;
import serveur.element.Caracteristique;
import serveur.vuelement.VuePersonnage;

public class Soigner extends Interaction<VuePersonnage> {

	public Soigner(Arene arene, VuePersonnage attaquant, VuePersonnage defenseur) {
		super(arene, attaquant, defenseur);
	}

	@Override
	public void interagit() {
		try {
			/* rendre 50 points de vie au defenseur */
			arene.incrementeCaractElement(defenseur, Caracteristique.VIE, 50);
			
		} //try
		catch (RemoteException e) {
			logs(Level.INFO, "\nErreur lors d'un soin : " + e.toString());
		} //catch
	}
	
}
