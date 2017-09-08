// Generated from IgniteSqlLexer.g4 by ANTLR 4.7

package org.apache.ignite.internal.parser;

import org.antlr.v4.runtime.Lexer;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.TokenStream;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.atn.*;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.misc.*;

@SuppressWarnings({"all", "warnings", "unchecked", "unused", "cast"})
public class IgniteSqlLexer extends Lexer {
	static { RuntimeMetaData.checkVersion("4.7", RuntimeMetaData.VERSION); }

	protected static final DFA[] _decisionToDFA;
	protected static final PredictionContextCache _sharedContextCache =
		new PredictionContextCache();
	public static final int
		SPACE=1, DROP=2, INDEX=3, IF=4, EXISTS=5, SEMI=6, DOT=7, ID=8, ID_LITERAL=9, 
		DQUOTE_STRING=10;
	public static String[] channelNames = {
		"DEFAULT_TOKEN_CHANNEL", "HIDDEN"
	};

	public static String[] modeNames = {
		"DEFAULT_MODE"
	};

	public static final String[] ruleNames = {
		"SPACE", "DROP", "INDEX", "IF", "EXISTS", "SEMI", "DOT", "ID", "ID_LITERAL", 
		"DQUOTE_STRING", "A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", 
		"L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", 
		"Z"
	};

	private static final String[] _LITERAL_NAMES = {
		null, null, null, null, null, null, "';'", "'.'"
	};
	private static final String[] _SYMBOLIC_NAMES = {
		null, "SPACE", "DROP", "INDEX", "IF", "EXISTS", "SEMI", "DOT", "ID", "ID_LITERAL", 
		"DQUOTE_STRING"
	};
	public static final Vocabulary VOCABULARY = new VocabularyImpl(_LITERAL_NAMES, _SYMBOLIC_NAMES);

	/**
	 * @deprecated Use {@link #VOCABULARY} instead.
	 */
	@Deprecated
	public static final String[] tokenNames;
	static {
		tokenNames = new String[_SYMBOLIC_NAMES.length];
		for (int i = 0; i < tokenNames.length; i++) {
			tokenNames[i] = VOCABULARY.getLiteralName(i);
			if (tokenNames[i] == null) {
				tokenNames[i] = VOCABULARY.getSymbolicName(i);
			}

			if (tokenNames[i] == null) {
				tokenNames[i] = "<INVALID>";
			}
		}
	}

	@Override
	@Deprecated
	public String[] getTokenNames() {
		return tokenNames;
	}

	@Override

	public Vocabulary getVocabulary() {
		return VOCABULARY;
	}


	public IgniteSqlLexer(CharStream input) {
		super(input);
		_interp = new LexerATNSimulator(this,_ATN,_decisionToDFA,_sharedContextCache);
	}

	@Override
	public String getGrammarFileName() { return "IgniteSqlLexer.g4"; }

	@Override
	public String[] getRuleNames() { return ruleNames; }

	@Override
	public String getSerializedATN() { return _serializedATN; }

	@Override
	public String[] getChannelNames() { return channelNames; }

	@Override
	public String[] getModeNames() { return modeNames; }

	@Override
	public ATN getATN() { return _ATN; }

	@Override
	public void action(RuleContext _localctx, int ruleIndex, int actionIndex) {
		switch (ruleIndex) {
		case 7:
			ID_action((RuleContext)_localctx, actionIndex);
			break;
		}
	}
	private void ID_action(RuleContext _localctx, int actionIndex) {
		switch (actionIndex) {
		case 0:
			 setText(getText().substring(1, getText().length() - 1)); 
			break;
		}
	}

	public static final String _serializedATN =
		"\3\u608b\ua72a\u8133\ub9ed\u417c\u3be7\u7786\u5964\2\f\u00b9\b\1\4\2\t"+
		"\2\4\3\t\3\4\4\t\4\4\5\t\5\4\6\t\6\4\7\t\7\4\b\t\b\4\t\t\t\4\n\t\n\4\13"+
		"\t\13\4\f\t\f\4\r\t\r\4\16\t\16\4\17\t\17\4\20\t\20\4\21\t\21\4\22\t\22"+
		"\4\23\t\23\4\24\t\24\4\25\t\25\4\26\t\26\4\27\t\27\4\30\t\30\4\31\t\31"+
		"\4\32\t\32\4\33\t\33\4\34\t\34\4\35\t\35\4\36\t\36\4\37\t\37\4 \t \4!"+
		"\t!\4\"\t\"\4#\t#\4$\t$\4%\t%\3\2\6\2M\n\2\r\2\16\2N\3\2\3\2\3\3\3\3\3"+
		"\3\3\3\3\3\3\4\3\4\3\4\3\4\3\4\3\4\3\5\3\5\3\5\3\6\3\6\3\6\3\6\3\6\3\6"+
		"\3\6\3\7\3\7\3\b\3\b\3\t\3\t\3\t\3\t\5\tp\n\t\3\n\3\n\7\nt\n\n\f\n\16"+
		"\nw\13\n\3\13\3\13\3\13\3\13\3\13\3\13\7\13\177\n\13\f\13\16\13\u0082"+
		"\13\13\3\13\3\13\3\f\3\f\3\r\3\r\3\16\3\16\3\17\3\17\3\20\3\20\3\21\3"+
		"\21\3\22\3\22\3\23\3\23\3\24\3\24\3\25\3\25\3\26\3\26\3\27\3\27\3\30\3"+
		"\30\3\31\3\31\3\32\3\32\3\33\3\33\3\34\3\34\3\35\3\35\3\36\3\36\3\37\3"+
		"\37\3 \3 \3!\3!\3\"\3\"\3#\3#\3$\3$\3%\3%\2\2&\3\3\5\4\7\5\t\6\13\7\r"+
		"\b\17\t\21\n\23\13\25\f\27\2\31\2\33\2\35\2\37\2!\2#\2%\2\'\2)\2+\2-\2"+
		"/\2\61\2\63\2\65\2\67\29\2;\2=\2?\2A\2C\2E\2G\2I\2\3\2 \5\2\13\f\17\17"+
		"\"\"\5\2C\\aac|\6\2\62;C\\aac|\4\2$$^^\4\2CCcc\4\2DDdd\4\2EEee\4\2FFf"+
		"f\4\2GGgg\4\2HHhh\4\2IIii\4\2JJjj\4\2KKkk\4\2LLll\4\2MMmm\4\2NNnn\4\2"+
		"OOoo\4\2PPpp\4\2QQqq\4\2RRrr\4\2SSss\4\2TTtt\4\2UUuu\4\2VVvv\4\2WWww\4"+
		"\2XXxx\4\2YYyy\4\2ZZzz\4\2[[{{\4\2\\\\||\2\u00a4\2\3\3\2\2\2\2\5\3\2\2"+
		"\2\2\7\3\2\2\2\2\t\3\2\2\2\2\13\3\2\2\2\2\r\3\2\2\2\2\17\3\2\2\2\2\21"+
		"\3\2\2\2\2\23\3\2\2\2\2\25\3\2\2\2\3L\3\2\2\2\5R\3\2\2\2\7W\3\2\2\2\t"+
		"]\3\2\2\2\13`\3\2\2\2\rg\3\2\2\2\17i\3\2\2\2\21o\3\2\2\2\23q\3\2\2\2\25"+
		"x\3\2\2\2\27\u0085\3\2\2\2\31\u0087\3\2\2\2\33\u0089\3\2\2\2\35\u008b"+
		"\3\2\2\2\37\u008d\3\2\2\2!\u008f\3\2\2\2#\u0091\3\2\2\2%\u0093\3\2\2\2"+
		"\'\u0095\3\2\2\2)\u0097\3\2\2\2+\u0099\3\2\2\2-\u009b\3\2\2\2/\u009d\3"+
		"\2\2\2\61\u009f\3\2\2\2\63\u00a1\3\2\2\2\65\u00a3\3\2\2\2\67\u00a5\3\2"+
		"\2\29\u00a7\3\2\2\2;\u00a9\3\2\2\2=\u00ab\3\2\2\2?\u00ad\3\2\2\2A\u00af"+
		"\3\2\2\2C\u00b1\3\2\2\2E\u00b3\3\2\2\2G\u00b5\3\2\2\2I\u00b7\3\2\2\2K"+
		"M\t\2\2\2LK\3\2\2\2MN\3\2\2\2NL\3\2\2\2NO\3\2\2\2OP\3\2\2\2PQ\b\2\2\2"+
		"Q\4\3\2\2\2RS\5\35\17\2ST\59\35\2TU\5\63\32\2UV\5\65\33\2V\6\3\2\2\2W"+
		"X\5\'\24\2XY\5\61\31\2YZ\5\35\17\2Z[\5\37\20\2[\\\5E#\2\\\b\3\2\2\2]^"+
		"\5\'\24\2^_\5!\21\2_\n\3\2\2\2`a\5\37\20\2ab\5E#\2bc\5\'\24\2cd\5;\36"+
		"\2de\5=\37\2ef\5;\36\2f\f\3\2\2\2gh\7=\2\2h\16\3\2\2\2ij\7\60\2\2j\20"+
		"\3\2\2\2kp\5\23\n\2lm\5\25\13\2mn\b\t\3\2np\3\2\2\2ok\3\2\2\2ol\3\2\2"+
		"\2p\22\3\2\2\2qu\t\3\2\2rt\t\4\2\2sr\3\2\2\2tw\3\2\2\2us\3\2\2\2uv\3\2"+
		"\2\2v\24\3\2\2\2wu\3\2\2\2x\u0080\7$\2\2yz\7^\2\2z\177\13\2\2\2{|\7$\2"+
		"\2|\177\7$\2\2}\177\n\5\2\2~y\3\2\2\2~{\3\2\2\2~}\3\2\2\2\177\u0082\3"+
		"\2\2\2\u0080~\3\2\2\2\u0080\u0081\3\2\2\2\u0081\u0083\3\2\2\2\u0082\u0080"+
		"\3\2\2\2\u0083\u0084\7$\2\2\u0084\26\3\2\2\2\u0085\u0086\t\6\2\2\u0086"+
		"\30\3\2\2\2\u0087\u0088\t\7\2\2\u0088\32\3\2\2\2\u0089\u008a\t\b\2\2\u008a"+
		"\34\3\2\2\2\u008b\u008c\t\t\2\2\u008c\36\3\2\2\2\u008d\u008e\t\n\2\2\u008e"+
		" \3\2\2\2\u008f\u0090\t\13\2\2\u0090\"\3\2\2\2\u0091\u0092\t\f\2\2\u0092"+
		"$\3\2\2\2\u0093\u0094\t\r\2\2\u0094&\3\2\2\2\u0095\u0096\t\16\2\2\u0096"+
		"(\3\2\2\2\u0097\u0098\t\17\2\2\u0098*\3\2\2\2\u0099\u009a\t\20\2\2\u009a"+
		",\3\2\2\2\u009b\u009c\t\21\2\2\u009c.\3\2\2\2\u009d\u009e\t\22\2\2\u009e"+
		"\60\3\2\2\2\u009f\u00a0\t\23\2\2\u00a0\62\3\2\2\2\u00a1\u00a2\t\24\2\2"+
		"\u00a2\64\3\2\2\2\u00a3\u00a4\t\25\2\2\u00a4\66\3\2\2\2\u00a5\u00a6\t"+
		"\26\2\2\u00a68\3\2\2\2\u00a7\u00a8\t\27\2\2\u00a8:\3\2\2\2\u00a9\u00aa"+
		"\t\30\2\2\u00aa<\3\2\2\2\u00ab\u00ac\t\31\2\2\u00ac>\3\2\2\2\u00ad\u00ae"+
		"\t\32\2\2\u00ae@\3\2\2\2\u00af\u00b0\t\33\2\2\u00b0B\3\2\2\2\u00b1\u00b2"+
		"\t\34\2\2\u00b2D\3\2\2\2\u00b3\u00b4\t\35\2\2\u00b4F\3\2\2\2\u00b5\u00b6"+
		"\t\36\2\2\u00b6H\3\2\2\2\u00b7\u00b8\t\37\2\2\u00b8J\3\2\2\2\b\2Nou~\u0080"+
		"\4\b\2\2\3\t\2";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}