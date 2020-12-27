package spot.notify.chatbot

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface Endpoints {

  @POST("/webhooks/rest/webhook")
  fun chatbot(@Body user: UserMessage): Call<List<BotResponse>>
}
