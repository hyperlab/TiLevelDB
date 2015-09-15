/**
 * Appcelerator Titanium Mobile
 * Copyright (c) 2009-2015 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the Apache Public License
 * Please see the LICENSE included with this distribution for details.
 */

#import "SeHyperlabLeveldbDatabaseProxy.h"

@implementation SeHyperlabLeveldbDatabaseProxy
@synthesize name;

-(LevelDB *)getDatabase
{
    if (_ldb == nil && name) {
        _ldb = [[LevelDB databaseInLibraryWithName:name] retain];
        
        _ldb.encoder = ^ NSData * (LevelDBKey *key, id object) {
            NSError *err = nil;
            NSData *data = [NSJSONSerialization dataWithJSONObject:object options:kNilOptions error:&err];
            return data;
        };
        _ldb.decoder = ^ id (LevelDBKey *key, NSData * data) {
            NSError *err = nil;
            id obj = [NSJSONSerialization JSONObjectWithData:data options:kNilOptions error:&err];
            return obj;
        };

        NSLog(@"Created database instance with name %@", name);
    }
    return _ldb;
}

-(void)setObject:(id)args
{
    NSString *key;
    NSObject *val;
    ENSURE_ARG_AT_INDEX(key, args, 0, NSString);
    ENSURE_ARG_AT_INDEX(val, args, 1, NSObject);

    
    [[self getDatabase] setObject:val forKey:key];
}

-(id)getObject:(id)args
{
    NSString *key;
    ENSURE_ARG_AT_INDEX(key, args, 0, NSString);
    
    id res = [[self getDatabase] objectForKey:key];
    return res;
}

-(BOOL)deleteObject:(id)args
{
    NSString *key;
    ENSURE_ARG_AT_INDEX(key, args, 0, NSString);
    
    [[self getDatabase] removeObjectForKey:key];
    return YES;
}

-(BOOL)deleteAllObjects:(id)args
{
    [[self getDatabase] removeAllObjects];
    return YES;
}


-(id)query:(id)args
{
    NSDictionary *props;
    ENSURE_ARG_AT_INDEX(props, args, 0, NSDictionary);

    NSString *prefix;
    ENSURE_ARG_FOR_KEY(prefix, props, @"prefix", NSString);
    
    NSUInteger prefixLength = [prefix length];
    NSMutableArray *res = [NSMutableArray array];
    
    [[self getDatabase] enumerateKeysAndObjectsBackward:NO
                                                lazily:NO
                                         startingAtKey:nil
                                   filteredByPredicate:nil
                                             andPrefix:prefix
                                            usingBlock:^(LevelDBKey *key, id obj, BOOL *stop) {
                                                NSString *k = NSStringFromLevelDBKey(key);
                                                NSString *tail;
                                                
                                                if ([k length] > prefixLength) {
                                                    tail = [k substringFromIndex:prefixLength];
                                                    
                                                    if ([tail rangeOfString:@"."].location == NSNotFound) {
                                                        [res addObject:obj];
                                                    }
                                                }
                                            }];
    
    return res;
}

#pragma mark Cleanup

-(void)dealloc
{
    // release any resources that have been retained by the module
    [super dealloc];
    if (_ldb != nil) {
        [_ldb release];
        _ldb = nil;
    }
}

@end
