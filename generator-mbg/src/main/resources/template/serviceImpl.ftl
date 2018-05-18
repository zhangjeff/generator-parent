package ${package}.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ${mapperPackage};
import ${genericMapperPackage};
import ${genericServiceImplPackage};
import ${baseModelPackage}.${domainName};
import ${baseModelPackage}.${domainName}Example;
import ${package}.${domainName}Service;

@Service("${smallDomainName}Service")
public class ${domainName}ServiceImpl extends GenericServiceImpl<${domainName}, ${domainName}Example, ${primaryKeyType}> implements ${domainName}Service {

	private static final Logger LOGGER = LoggerFactory.getLogger(${domainName}ServiceImpl.class);
	
	@Autowired
	private ${domainName}Mapper ${smallDomainName}Mapper;
	
	@Override
	protected GenericMapper<${domainName}, ${domainName}Example, ${primaryKeyType}> getGenericMapper() {
		return ${smallDomainName}Mapper;
	}
}
