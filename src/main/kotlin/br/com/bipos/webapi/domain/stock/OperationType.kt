package br.com.bipos.webapi.domain.stock

enum class OperationType {
    // 🏟️ ESPAÇOS COM GRANDE CIRCULAÇÃO
    STADIUM,           // Estádio de futebol - vendedores ambulantes
    ARENA,             // Arena de shows e eventos
    CONVENTION_CENTER, // Centro de convenções
    FAIR,              // Feira/exposição
    CIRCUS,            // Circo - venda de pipoca, algodão doce

    // 🎵 SHOWS E ENTRETENIMENTO
    CONCERT,           // Show musical
    FESTIVAL,          // Festival (multidias, múltiplos palcos)
    NIGHT_CLUB,        // Casa noturna/balada
    THEATER,           // Teatro - intervalo

    // 🍔 ALIMENTAÇÃO FORA DO LAR
    FOOD_TRUCK,        // Food truck em eventos
    TEMPORARY_BAR,     // Bar temporário em eventos
    KIOSK,             // Quiosque em shopping/evento

    // 🏖️ TURISMO E LAZER
    BEACH,             // Praia - vendedor de bebidas
    PARK,              // Parque - vendedor ambulante
    TOURIST_SPOT,      // Ponto turístico

    // ⚽ ESPORTES
    SOCCER_MATCH,      // Jogo de futebol
    BASKETBALL_GAME,   // Jogo de basquete
    VOLLEYBALL_GAME,   // Jogo de vôlei
    FIGHT_EVENT,       // Evento de luta (UFC, Boxe)

    // 🎪 EVENTOS TEMPORÁRIOS
    CHRISTMAS_MARKET,  // Feira de Natal
    CARNIVAL_STREET,   // Rua de Carnaval
    JUNE_FESTIVAL,     // Festa Junina
    OKTOBERFEST,       // Oktoberfest

    // 🚇 MOBILIDADE URBANA
    SUBWAY_STATION,    // Estação de metrô
    BUS_STATION,       // Rodoviária
    AIRPORT,           // Aeroporto

    // 🏢 OUTROS
    OTHER              // Outros
}