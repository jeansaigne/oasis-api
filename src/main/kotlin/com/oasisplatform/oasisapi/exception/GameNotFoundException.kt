package com.oasisplatform.oasisapi.exception

class GameNotFoundException(id: Long) : RuntimeException("Jeu non trouvé avec l'id: $id")
