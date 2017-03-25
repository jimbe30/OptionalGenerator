package com.helospark.spark.builder.handlers.codegenerator.component.fragment.builderclass.stagedinterface;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclaration;

import com.helospark.spark.builder.handlers.codegenerator.component.fragment.builderclass.buildmethod.BuildMethodDeclarationCreatorFragment;
import com.helospark.spark.builder.handlers.codegenerator.component.fragment.builderclass.withmethod.StagedBuilderMethodDefiniationCreatorFragment;
import com.helospark.spark.builder.handlers.codegenerator.component.helper.GeneratedAnnotationPopulator;
import com.helospark.spark.builder.handlers.codegenerator.component.helper.JavadocAdder;
import com.helospark.spark.builder.handlers.codegenerator.component.helper.StagedBuilderProperties;
import com.helospark.spark.builder.handlers.codegenerator.domain.CompilationUnitModificationDomain;
import com.helospark.spark.builder.handlers.codegenerator.domain.NamedVariableDeclarationField;

public class StagedBuilderInterfaceCreatorFragment {
    private StagedBuilderInterfaceTypeDefinitionCreatorFragment interfaceDefinitionCreator;
    private StagedBuilderMethodDefiniationCreatorFragment stagedBuilderMethodDefiniationCreatorFragment;
    private BuildMethodDeclarationCreatorFragment buildMethodDeclarationCreatorFragment;
    private JavadocAdder javadocAdder;
    private GeneratedAnnotationPopulator generatedAnnotationPopulator;

    public StagedBuilderInterfaceCreatorFragment(StagedBuilderMethodDefiniationCreatorFragment stagedBuilderMethodDefiniationCreatorFragment,
            StagedBuilderInterfaceTypeDefinitionCreatorFragment stagedBuilderInterfaceTypeDefinitionCreatorFragment,
            StagedBuilderInterfaceTypeDefinitionCreatorFragment interfaceDefinitionCreator,
            BuildMethodDeclarationCreatorFragment buildMethodDeclarationCreatorFragment, JavadocAdder javadocAdder,
            GeneratedAnnotationPopulator generatedAnnotationPopulator) {
        this.stagedBuilderMethodDefiniationCreatorFragment = stagedBuilderMethodDefiniationCreatorFragment;
        this.buildMethodDeclarationCreatorFragment = buildMethodDeclarationCreatorFragment;
        this.javadocAdder = javadocAdder;
        this.interfaceDefinitionCreator = interfaceDefinitionCreator;
        this.generatedAnnotationPopulator = generatedAnnotationPopulator;
    }

    public TypeDeclaration createInterfaceFor(CompilationUnitModificationDomain modificationDomain,
            StagedBuilderProperties stagedBuilderProperties) {
        String interfaceName = stagedBuilderProperties.getInterfaceName();
        AST ast = modificationDomain.getAst();

        TypeDeclaration addedInterface = interfaceDefinitionCreator.createStageBuilderInterface(ast, interfaceName);
        addWithMethods(ast, stagedBuilderProperties, addedInterface);
        if (stagedBuilderProperties.isBuildStage()) {
            addBuildMethod(modificationDomain, addedInterface);
        }
        generatedAnnotationPopulator.addGeneratedAnnotationOnStagedBuilderInterface(ast, addedInterface);

        return addedInterface;
    }

    private void addBuildMethod(CompilationUnitModificationDomain modificationDomain, TypeDeclaration addedInterface) {
        AST ast = modificationDomain.getAst();
        TypeDeclaration originalType = modificationDomain.getOriginalType();
        MethodDeclaration buildMethod = buildMethodDeclarationCreatorFragment.createMethod(ast,
                originalType);
        javadocAdder.addJavadocForBuildMethod(ast, buildMethod);
        addedInterface.bodyDeclarations().add(buildMethod);
    }

    private void addWithMethods(AST ast, StagedBuilderProperties stagedBuilderProperties, TypeDeclaration addedInterface) {
        StagedBuilderProperties nextStage = getNextStage(stagedBuilderProperties);
        for (NamedVariableDeclarationField field : stagedBuilderProperties.getNamedVariableDeclarationField()) {

            MethodDeclaration withMethodDeclaration = stagedBuilderMethodDefiniationCreatorFragment.createNewWithMethod(
                    ast,
                    field,
                    nextStage);
            javadocAdder.addJavadocForWithMethod(ast, field.getBuilderFieldName(), withMethodDeclaration);
            addedInterface.bodyDeclarations().add(withMethodDeclaration);
        }
    }

    private StagedBuilderProperties getNextStage(StagedBuilderProperties stagedBuilderProperties) {
        return stagedBuilderProperties.isBuildStage() ? stagedBuilderProperties
                : stagedBuilderProperties.getNextStage().get();
    }

}
