package com.oasisplatform.oasisapi

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class OasisApiApplication

fun main(args: Array<String>) {
	runApplication<OasisApiApplication>(*args)
}
