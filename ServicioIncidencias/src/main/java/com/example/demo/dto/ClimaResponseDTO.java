package com.example.demo.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ClimaResponseDTO {
	
	// OpenWeather devuelve un arreglo "weather"
    private List<WeatherInfo> weather;
    
    // Método auxiliar para obtener la descripción principal 
    public String getCondicionPrincipal() {
        if (weather != null && !weather.isEmpty()) {
            return weather.get(0).getDescription(); 
        }
        return "Desconocido";
    }
}

