/*
 * MMDBot - https://github.com/MinecraftModDevelopment/MMDBot
 * Copyright (C) 2016-2023 <MMD - MinecraftModDevelopment>
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation;
 * Specifically version 2.1 of the License.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301
 * USA
 * https://www.gnu.org/licenses/old-licenses/lgpl-2.1.html
 */
package com.mcmoddev.mmdbot.commander.docs;

import com.google.common.collect.Lists;
import de.ialistannen.javadocapi.model.JavadocElement;
import de.ialistannen.javadocapi.model.QualifiedName;
import de.ialistannen.javadocapi.model.types.JavadocType;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class CustomDocFormatter implements DocsEmbed.DocFormatter {
    @Override
    public String format(final JavadocElement element, final JavadocElement.DeclarationStyle style) {
        if (element instanceof JavadocType type) {
            return formatType(type, style);
        }

        return element.getDeclaration(style);
    }

    public String formatType(final JavadocType type, final JavadocElement.DeclarationStyle style) {
        final var result = new StringBuilder();

        if (!type.getAnnotations().isEmpty()) {
            result.append(type.getAnnotations().stream()
                .map(it -> it.getDeclaration(style))
                .collect(Collectors.joining("\n")));
            result.append("\n");
        }

        result.append(String.join(" ", filterModifiers(type))).append(" ");
        result.append(type.getType().getKeyword()).append(" ");

        result.append(type.getQualifiedName().formatted(style));

        if (!type.getTypeParameters().isEmpty()) {
            result.append(type.getTypeParameters().stream()
                .map(it -> it.getDeclaration(style))
                .collect(Collectors.joining(", ", "<", ">")));
        }

        if (type.getType() == JavadocType.Type.ANNOTATION) {
            final var members = type.getMembers().stream()
                    .flatMap(it -> getMethodName(it).stream())
                    .filter(QualifiedName::isMethod)
                    .map(QualifiedName::asString)
                    .toList();
            if (!members.isEmpty()) {
                final var partitioned = Lists.partition(members, 5);
                result.append("(\n");
                result.append(String.join("\n", partitioned.stream()
                    .map(it -> "  " + String.join(", ", it))
                    .toList()));
                result.append("\n)");
            }
        }

        if (type.getSuperClass() != null) {
            result.append(" extends ").append(type.getSuperClass().getDeclaration(style));
        }

        if (!type.getSuperInterfaces().isEmpty()) {
            result.append(" implements ").append(type.getSuperInterfaces().stream()
                .map(it -> it.getDeclaration(style))
                .collect(Collectors.joining(", ")));
        }

        return result.toString();
    }

    private List<String> filterModifiers(final JavadocType type) {
        final var modifiers = new ArrayList<>(type.getModifiers());
        if (type.getType() == JavadocType.Type.INTERFACE || type.getType() == JavadocType.Type.ANNOTATION) {
            modifiers.remove("abstract");
        }
        return modifiers;
    }

    public Optional<QualifiedName> getMethodName(QualifiedName qualifiedName) {
        if (qualifiedName.asString().contains("#")) {
            return Optional.of(new QualifiedName(
                qualifiedName.asString().substring(qualifiedName.asString().indexOf("#") + 1),
                qualifiedName.getModuleName().orElse(null)
            ));
        }
        return Optional.empty();
    }
}
