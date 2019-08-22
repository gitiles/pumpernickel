package com.pump.data.operator;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.Objects;

import com.pump.text.WildcardPattern;

public class Like extends AbstractValueOperator<WildcardPattern> {
	private static final long serialVersionUID = 1L;

	public static String FUNCTION_NAME = "matches";

	public Like(String attribute, WildcardPattern pattern) {
		super(attribute, pattern);
		Objects.requireNonNull(pattern);
	}

	@Override
	public boolean evaluate(OperatorContext context, Object bean)
			throws Exception {
		Object value = context.getValue(bean, getAttribute());
		if (value == null)
			return false;
		String str = String.valueOf(value);
		return getValue().matches(str);
	}

	@Override
	protected String toString(boolean negated) {
		StringBuilder sb = new StringBuilder();
		if (negated)
			sb.append("!");
		sb.append(FUNCTION_NAME);
		sb.append("(");
		sb.append(getAttribute());
		sb.append(", \"");
		sb.append(getValue().getPatternText());
		sb.append("\")");
		return sb.toString();
	}

	@Override
	protected Operator createCanonicalOperator() {
		WildcardPattern p = getValue();

		if (p.getFormat().caseSensitive) {
			StringBuilder fixedString = new StringBuilder();
			for (WildcardPattern.Placeholder placeholder : p.getPlaceholders()) {
				if (placeholder instanceof WildcardPattern.FixedCharacter) {
					if (fixedString != null) {
						WildcardPattern.FixedCharacter f = (WildcardPattern.FixedCharacter) placeholder;
						fixedString.append(f.ch);
					}
				} else {
					fixedString = null;
				}
			}

			if (fixedString != null && fixedString.length() > 0) {
				return new EqualTo(getAttribute(), fixedString.toString());
			}
		}

		if (p.getPlaceholderCount() == 1
				&& p.getPlaceholders()[0] instanceof WildcardPattern.StarWildcard)
			return new Not(new EqualTo(getAttribute(), null));

		return this;
	}

	@Override
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public Collection<Operator> split() {
		return (Collection) Collections.singleton(this);
	}

	private void writeObject(java.io.ObjectOutputStream out) throws IOException {
		out.writeInt(0);
	}

	private void readObject(java.io.ObjectInputStream in) throws IOException,
			ClassNotFoundException {
		int version = in.readInt();
		if (version == 0) {
		} else {
			throw new IOException("Unsupported internal version: " + version);
		}
	}
}