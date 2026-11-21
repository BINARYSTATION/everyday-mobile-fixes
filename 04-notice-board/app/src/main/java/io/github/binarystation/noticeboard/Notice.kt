package io.github.binarystation.noticeboard

/** One pinned notice. All of them are made up for this demo. */
data class Notice(
    val id: Int,
    val title: String,
    val postedBy: String,
    val body: String,
)

object Notices {
    val all: List<Notice> = listOf(
        Notice(
            id = 1,
            title = "Water tank cleaning on Sunday",
            postedBy = "Society office",
            body = "The overhead tanks of all three blocks will be cleaned on Sunday, 9 am to 12 noon. " +
                "Please store water for the morning.",
        ),
        Notice(
            id = 2,
            title = "Lift service at 2 pm",
            postedBy = "Block B committee",
            body = "The Block B lift will be serviced at 2 pm on Tuesday. " +
                "Please use the stairs or the Block A lift for about 45 minutes.",
        ),
        Notice(
            id = 3,
            title = "Parking stickers due Friday",
            postedBy = "Parking committee",
            body = "Every car needs the new sticker by Friday evening. " +
                "Collect it from the guard room and bring the registration number.",
        ),
        Notice(
            id = 4,
            title = "Chai stall opens at the gate",
            postedBy = "Society office",
            body = "A small chai stall opens near the main gate on Monday at 7 am. " +
                "Cups are paper, please return the glasses.",
        ),
        Notice(
            id = 5,
            title = "Rangoli contest, Saturday 5 pm",
            postedBy = "Clubhouse team",
            body = "Bring your own colors. The contest is at the clubhouse lawn, " +
                "and the winner gets a box of laddoos.",
        ),
        Notice(
            id = 6,
            title = "Lost: one cricket ball",
            postedBy = "Kids' cricket group",
            body = "Red ball, last seen going over the wall of Block C. " +
                "If you have it, please leave it at the guard room. No questions asked.",
        ),
    )

    fun byId(id: Int): Notice = all.first { it.id == id }
}
