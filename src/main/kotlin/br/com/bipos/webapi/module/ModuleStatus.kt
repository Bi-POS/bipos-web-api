package br.com.bipos.webapi.module

enum class ModuleStatus {
    ACTIVE,      // Empresa tem e está ativo
    INACTIVE,    // Empresa tem mas está desativado
    NOT_ASSIGNED,// Empresa não contratou
    NOT_FOUND    // Módulo não existe no sistema
}