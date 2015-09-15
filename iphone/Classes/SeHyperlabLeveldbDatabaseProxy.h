/**
 * Appcelerator Titanium Mobile
 * Copyright (c) 2009-2015 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the Apache Public License
 * Please see the LICENSE included with this distribution for details.
 */
#import "TiProxy.h"
#import "LevelDB.h"

@interface SeHyperlabLeveldbDatabaseProxy : TiProxy {
    LevelDB *_ldb;
}
@property (retain) NSString *name;

@end
