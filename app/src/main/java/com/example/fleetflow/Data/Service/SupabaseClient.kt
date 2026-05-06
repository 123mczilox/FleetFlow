package com.example.fleetflow.Data.Service

import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.realtime.Realtime
import io.github.jan.supabase.storage.Storage

object SupabaseClient {
    val client = createSupabaseClient(
        supabaseUrl = "https://wykylclzeeqmuvqaitcw.supabase.co",
        supabaseKey = "sb_publishable_M-lzzNYKqgyec8n5hoCPrQ_L_q08vLk"
    ) {
        install(Postgrest)
        install(Auth)
        install(Storage)
        install(Realtime)
    }
}
