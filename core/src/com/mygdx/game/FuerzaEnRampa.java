package com.mygdx.game;

import java.util.Scanner;

import java.util.Scanner;

public class FuerzaEnRampa {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        
        System.out.print("Ingrese la masa del cuerpo (kg): ");
        double masa = scanner.nextDouble();
        
        System.out.print("Ingrese el ángulo de la pendiente (grados): ");
        double angulo = scanner.nextDouble();
        
        // Convertir el ángulo a radianes
        double anguloRadianes = Math.toRadians(angulo);
        
        // Aceleración debido a la gravedad
        double gravedad = 9.8; // m/s^2
        
        // Componente x de la fuerza requerida para contrarrestar la gravedad a lo largo de la rampa
        double fuerzaRampaX = -masa * gravedad * Math.sin(anguloRadianes);
        
        // Componente y de la fuerza requerida para contrarrestar la componente perpendicular de la gravedad
        double fuerzaPerpendicularY = masa * gravedad * Math.cos(anguloRadianes);
        
        System.out.println("Fuerza requerida en la rampa (x, y): (" + fuerzaRampaX + " N, " + fuerzaPerpendicularY + " N)");
        
        scanner.close();
    }
}
