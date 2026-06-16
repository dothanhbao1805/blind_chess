package board

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.IntOffset
import com.russhwolf.settings.set
import kotlinx.datetime.Clock
import pieces.Bishop
import pieces.King
import pieces.Knight
import pieces.Pawn
import pieces.Piece
import pieces.Rook
import pieces.Queen
import settings.boardSettings

@Composable
fun rememberBoard(
    encodedPieces: String = InitialEncodedPiecesPosition,
    key: Int = 0,
): Board =
    remember(key) {  // thêm key vào remember
        Board(encodedPieces = encodedPieces)
    }

@Immutable
class Board(
    encodedPieces: String = InitialEncodedPiecesPosition,
) {
    private val _pieces = mutableStateListOf<Piece>()
    val pieces get() = _pieces.toList()

    init {
        _pieces.addAll(
            decodePieces(encodedPieces = encodedPieces)
        )
        assignHiddenPieces()
    }

    var selectedPiece by mutableStateOf<Piece?>(null)
        private set

    var selectedPieceMoves by mutableStateOf(emptySet<IntOffset>())
        private set

    var moveIncrement by mutableIntStateOf(0)
        private set

    var playerTurn by mutableStateOf(Piece.Color.White)

    var isGameOver by mutableStateOf(false)
        private set

    var winner by mutableStateOf<Piece.Color?>(null)
        private set

    /**
     * User events
     */

    fun selectPiece(piece: Piece) {
        if (piece.color != playerTurn) return

        if (piece == selectedPiece) {
            clearSelection()
        } else {
            selectedPiece = piece

            if (!piece.isRevealed) {
                // Chưa lật → đi theo luật của chính piece đó (vị trí hiện tại)
                selectedPieceMoves = piece.getAvailableMoves(pieces = pieces)
            } else {
                // Đã lật → đi theo luật của realPiece (hoặc chính nó nếu realPiece null)
                val effectivePiece = piece.realPiece ?: piece
                effectivePiece.position = piece.position
                selectedPieceMoves = effectivePiece.getAvailableMoves(pieces = pieces)
            }
        }
    }

    fun moveSelectedPiece(x: Int, y: Int) {
        selectedPiece?.let { piece ->
            if (!isAvailableMove(x = x, y = y))
                return

            if (piece.color != playerTurn)
                return

            movePiece(
                piece = piece,
                position = IntOffset(x, y)
            )

            clearSelection()

            switchPlayerTurn()

            moveIncrement++
        }
    }

    /**
     * Public Methods
     */

    fun getPiece(x: Int, y: Int): Piece? =
        _pieces.find { it.position.x == x && it.position.y == y }

    fun isAvailableMove(x: Int, y: Int): Boolean =
        selectedPieceMoves.any { it.x == x && it.y == y }

    fun save() {
        val encodedBoard = encode()
        val millis = Clock.System.now().toEpochMilliseconds()

        boardSettings[BoardKeyPrefix + millis] = encodedBoard
    }

    /**
     * Private Methods
     */

    private fun movePiece(piece: Piece, position: IntOffset) {
        val targetPiece = pieces.find { it.position == position }

        if (targetPiece != null) {
            // Kiểm tra nếu ăn King thật (realPiece là King hoặc chính nó là King)
            val realTarget = targetPiece.realPiece ?: targetPiece
            if (realTarget is King) {
                isGameOver = true
                winner = piece.color
            }
            removePiece(targetPiece)
        }

        piece.position = position
        piece.isRevealed = true

        piece.realPiece?.let { real ->
            real.position = position
            real.isRevealed = true
            _pieces.remove(piece)
            _pieces.add(real)
        }
    }

    private fun assignHiddenPieces() {
        val whitePieces = _pieces.filter { it.color == Piece.Color.White }
        val blackPieces = _pieces.filter { it.color == Piece.Color.Black }

        assignRandomPieces(whitePieces, Piece.Color.White)
        assignRandomPieces(blackPieces, Piece.Color.Black)
    }

    private fun assignRandomPieces(pieces: List<Piece>, color: Piece.Color) {
        // Tạo pool quân thật theo đúng số lượng cờ vua
        val pool = mutableListOf<Piece>().apply {
            add(King(color, IntOffset(0, 0)))
            add(Queen(color, IntOffset(0, 0)))
            add(Rook(color, IntOffset(0, 0)))
            add(Rook(color, IntOffset(0, 0)))
            add(Bishop(color, IntOffset(0, 0)))
            add(Bishop(color, IntOffset(0, 0)))
            add(Knight(color, IntOffset(0, 0)))
            add(Knight(color, IntOffset(0, 0)))
            repeat(8) { add(Pawn(color, IntOffset(0, 0))) }
        }.shuffled()

        pieces.forEachIndexed { index, piece ->
            piece.realPiece = pool[index]
        }
    }

    private fun removePiece(piece: Piece) {
        _pieces.remove(piece)
    }


    private fun clearSelection() {
        selectedPiece = null
        selectedPieceMoves = emptySet()
    }

    private fun switchPlayerTurn() {
        playerTurn =
            if (playerTurn.isWhite)
                Piece.Color.Black
            else
                Piece.Color.White
    }

    private fun encode(): String {
        return pieces.joinToString(separator = "") { it.encode() }
    }

    companion object {
        const val BoardKeyPrefix = "board_"
    }
}

@Composable
fun Board.rememberPieceAt(x: Int, y: Int): Piece? =
    remember(x, y, moveIncrement) {
        getPiece(
            x = x,
            y = y,
        )
    }

@Composable
fun Board.rememberIsAvailableMove(x: Int, y: Int): Boolean =
    remember(x, y, selectedPieceMoves) {
        isAvailableMove(
            x = x,
            y = y,
        )
    }